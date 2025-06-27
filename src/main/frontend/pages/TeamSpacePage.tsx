import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate, Link, useLocation } from "react-router-dom";
import {
  Button,
  Input,
  Card,
  Modal,
  TextArea,
  CalendarDaysIcon,
  PlusCircleIcon,
  TrashIcon,
} from "../components";
import {
  TeamProject,
  CalendarEvent,
  User,
  KanbanBoard,
  KanbanCard as KanbanCardType,
  KanbanComment,
  BulletinPost,
  BulletinComment,
} from "../types";
import { useAuth } from "../AuthContext";
import {
  CheckCircleIcon,
  Bars3Icon,
  TableCellsIcon,
  ClipboardDocumentListIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ChatBubbleBottomCenterTextIcon,
} from "@heroicons/react/24/outline";
import { postApi, commentApi, scheduleApi, fileApi } from "../services/api";
import {
  PostResponse,
  CommentResponse,
  ScheduleResponse,
  PostCreateRequest,
  CommentCreateRequest,
  ScheduleCreateRequest,
  ScheduleUpdateRequest,
} from "../services/types";

// Mock Team Data - in a real app, this would be fetched
const MOCK_TEAM_PROJECTS_ALL_DETAIL: TeamProject[] = [
  {
    id: "tp_alpha",
    workspaceId: "ws_kosta",
    name: "알파 프로젝트 팀",
    members: [
      {
        id: "user_kim",
        name: "김코딩",
        email: "kim@example.com",
        profilePictureUrl: "https://picsum.photos/seed/userA/40/40",
      },
      {
        id: "user_park",
        name: "박해커",
        email: "park@example.com",
        profilePictureUrl: "https://picsum.photos/seed/userB/40/40",
      },
    ],
    announcements: [
      {
        id: "anno1",
        content: "이번 주 금요일 오후 3시, 주간 회의 진행합니다.",
        author: "김코딩",
        timestamp: new Date(Date.now() - 86400000),
      },
      {
        id: "anno2",
        content: "프로젝트 중간 발표 자료 준비해주세요.",
        author: "박해커",
        timestamp: new Date(),
      },
    ],
    passwordProtected: false,
    progress: 75,
  },
  {
    id: "tp_beta",
    workspaceId: "ws_kosta",
    name: "베타 서비스 개발팀",
    members: [
      {
        id: "user_lee",
        name: "이디자인",
        email: "lee@example.com",
        profilePictureUrl: "https://picsum.photos/seed/userC/40/40",
      },
      {
        id: "user_choi",
        name: "최기획",
        email: "choi@example.com",
        profilePictureUrl: "https://picsum.photos/seed/userD/40/40",
      },
    ],
    announcements: [
      {
        id: "anno3",
        content: "1차 디자인 시안 공유드립니다. 피드백 부탁해요!",
        author: "이디자인",
        timestamp: new Date(),
      },
    ],
    progress: 40,
  },
  {
    id: "tp_gongji_features",
    workspaceId: "ws_kosta",
    name: "#공지 전용 (기능)", // Example: a "channel" that might have Kanban/Bulletin beyond just chat
    members: [],
    announcements: [
      {
        id: "gongji1",
        content: "전체 워크스페이스 공지입니다.",
        author: "관리자",
        timestamp: new Date(),
      },
    ],
    progress: 100,
  },
];

// Sub-components for TeamSpacePage
const TeamAnnouncementBoard: React.FC<{
  teamProjectId: string;
  currentUser: User;
}> = ({ teamProjectId, currentUser }) => {
  const [announcements, setAnnouncements] = useState<BulletinPost[]>([]);
  const [newAnnouncement, setNewAnnouncement] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [uploading, setUploading] = useState(false);

  // 팀 프로젝트 ID를 백엔드 팀 ID로 변환하는 헬퍼 함수
  const getTeamId = (teamProjectId: string): number => {
    switch (teamProjectId) {
      case "tp_alpha":
        return 1;
      case "tp_beta":
        return 2;
      case "tp_personal":
        return 3;
      case "tp_gongji_features":
        return 4;
      default:
        return 1;
    }
  };

  // 공지사항 로드 (공지사항 전용 보드 사용)
  const loadAnnouncements = useCallback(async () => {
    try {
      setLoading(true);
      // 공지사항용 보드 ID는 2로 설정 (게시판과 분리)
      const teamId = getTeamId(teamProjectId);
      const response = await postApi.getPosts(teamId, 2, 0, 50);
      const bulletinPosts: BulletinPost[] = response.content.map(
        (post: PostResponse) => ({
          id: post.id.toString(),
          teamProjectId: teamProjectId,
          title: post.title,
          content: post.content,
          authorId: post.authorId.toString(),
          authorName: post.authorName,
          createdAt: new Date(post.createdAt),
          updatedAt: post.updatedAt ? new Date(post.updatedAt) : undefined,
          attachments:
            post.attachments?.map((att) => ({
              id: att.id.toString(),
              postId: post.id.toString(),
              fileName: att.originalName || `첨부파일_${att.id}`, // originalName이 없을 경우 기본 이름 제공
              fileUrl: `/files/${att.id}/download`, // 다운로드 URL 수정
            })) || [],
          comments: [],
        })
      );
      setAnnouncements(bulletinPosts);
    } catch (error) {
      console.error("공지사항 로드 실패:", error);
      // API 호출 실패 시 빈 배열 설정 (실제 환경에서는 mock 데이터 사용 가능)
      setAnnouncements([]);
    } finally {
      setLoading(false);
    }
  }, [teamProjectId]);

  useEffect(() => {
    loadAnnouncements();
  }, [loadAnnouncements]);

  const handleAdd = async () => {
    if (newAnnouncement.trim()) {
      try {
        setUploading(true);
        const accountId = parseInt(currentUser.id) || 1;
        const teamId = getTeamId(teamProjectId);

        if (selectedFiles.length > 0) {
          // 파일이 있는 경우
          const postData: PostCreateRequest = {
            title: "공지사항",
            content: newAnnouncement.trim(),
            boardId: 2, // 공지사항용 보드 ID (게시판과 분리)
          };

          await fileApi.createPostWithAttachments(
            teamId,
            accountId,
            postData,
            selectedFiles
          );
        } else {
          // 파일이 없는 경우
          const postData: PostCreateRequest = {
            title: "공지사항",
            content: newAnnouncement.trim(),
            boardId: 2, // 공지사항용 보드 ID (게시판과 분리)
          };

          await postApi.createPost(teamId, accountId, postData);
        }

        // 새로 생성된 공지사항을 다시 로드
        await loadAnnouncements();
        setNewAnnouncement("");
        setSelectedFiles([]);
        setShowModal(false);
      } catch (error) {
        console.error("공지사항 추가 실패:", error);
        alert("공지사항 추가에 실패했습니다.");
      } finally {
        setUploading(false);
      }
    }
  };

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (files) {
      setSelectedFiles(Array.from(files));
    }
  };

  const handleRemoveFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleDelete = async (announcementId: string) => {
    if (window.confirm("이 공지사항을 정말 삭제하시겠습니까?")) {
      try {
        const accountId = parseInt(currentUser.id) || 1;
        await postApi.deletePost(parseInt(announcementId), accountId);
        await loadAnnouncements(); // 삭제 후 다시 로드
      } catch (error) {
        console.error("공지사항 삭제 실패:", error);
        alert("공지사항 삭제에 실패했습니다.");
      }
    }
  };

  return (
    <Card
      title="📢 팀 공지사항"
      actions={
        <Button
          size="sm"
          onClick={() => setShowModal(true)}
          leftIcon={<PlusCircleIcon />}
        >
          공지 추가
        </Button>
      }
    >
      {loading ? (
        <p className="text-neutral-500">공지사항을 불러오는 중...</p>
      ) : announcements.length === 0 ? (
        <p className="text-neutral-500">아직 공지사항이 없습니다.</p>
      ) : (
        <ul className="space-y-3">
          {announcements
            .slice()
            .reverse()
            .map((announcement) => (
              <li
                key={announcement.id}
                className="p-4 bg-yellow-50 border-l-4 border-yellow-400 rounded-md shadow-sm group"
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                        📢 공지
                      </span>
                      <span className="text-xs text-neutral-500">
                        {announcement.createdAt.toLocaleString()}
                      </span>
                    </div>
                    <h4 className="font-semibold text-neutral-800 mb-2">
                      {announcement.title}
                    </h4>
                    <p className="text-neutral-700 whitespace-pre-line mb-2">
                      {announcement.content}
                    </p>
                    <div className="flex items-center justify-between">
                      <p className="text-xs text-neutral-500">
                        작성자: {announcement.authorName}
                      </p>
                      {announcement.attachments &&
                        announcement.attachments.length > 0 && (
                          <div className="flex items-center space-x-2">
                            <span className="text-xs text-neutral-500">
                              첨부파일:
                            </span>
                            {announcement.attachments.map((attachment) => (
                              <a
                                key={attachment.id}
                                href={attachment.fileUrl}
                                download={attachment.fileName}
                                className="text-xs text-primary hover:underline"
                              >
                                📎 {attachment.fileName}
                              </a>
                            ))}
                          </div>
                        )}
                    </div>
                  </div>
                  {currentUser.id === announcement.authorId && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(announcement.id)}
                      className="opacity-0 group-hover:opacity-100 transition-opacity ml-2"
                      aria-label="공지 삭제"
                    >
                      <TrashIcon className="w-4 h-4 text-red-500" />
                    </Button>
                  )}
                </div>
              </li>
            ))}
        </ul>
      )}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="새 공지사항 작성"
        footer={
          <div className="flex justify-end space-x-2">
            <Button
              variant="ghost"
              onClick={() => setShowModal(false)}
              disabled={uploading}
            >
              취소
            </Button>
            <Button onClick={handleAdd} disabled={uploading}>
              {uploading ? "등록 중..." : "등록"}
            </Button>
          </div>
        }
      >
        <div className="space-y-3">
          <TextArea
            label="공지 내용"
            value={newAnnouncement}
            onChange={(e) => setNewAnnouncement(e.target.value)}
            placeholder="공지 내용을 입력하세요..."
            rows={4}
            required
          />
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-1">
              첨부파일 (선택)
            </label>
            <Input
              type="file"
              multiple
              className="text-sm"
              onChange={handleFileSelect}
            />
            {selectedFiles.length > 0 && (
              <div className="mt-2">
                <span className="text-sm font-medium text-neutral-700">
                  선택된 파일:
                </span>
                <ul className="list-disc list-inside text-xs mt-1">
                  {selectedFiles.map((file, index) => (
                    <li
                      key={index}
                      className="flex justify-between items-center"
                    >
                      <span className="text-neutral-700">{file.name}</span>
                      <Button
                        variant="ghost"
                        size="xs"
                        onClick={() => handleRemoveFile(index)}
                        className="opacity-70 hover:opacity-100"
                        aria-label="파일 제거"
                      >
                        <TrashIcon className="w-4 h-4 text-red-500" />
                      </Button>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      </Modal>
    </Card>
  );
};

// TeamVideoConference component removed from here.

const getDaysInMonth = (year: number, month: number) => {
  return new Date(year, month + 1, 0).getDate();
};

const getFirstDayOfMonth = (year: number, month: number) => {
  return new Date(year, month, 1).getDay();
};

const daysOfWeek = ["일", "월", "화", "수", "목", "금", "토"];

const TeamCalendar: React.FC<{
  teamProjectId: string;
  currentUser: User;
}> = ({ teamProjectId, currentUser }) => {
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [showAddEventModal, setShowAddEventModal] = useState(false);
  const [newEvent, setNewEvent] = useState<Partial<CalendarEvent>>({
    title: "",
    start: new Date(),
    end: new Date(),
    type: "meeting",
    teamProjectId,
  });
  const [editingEventId, setEditingEventId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const [currentDisplayDate, setCurrentDisplayDate] = useState(new Date());
  const year = currentDisplayDate.getFullYear();
  const month = currentDisplayDate.getMonth();

  // 팀 프로젝트 ID를 백엔드 팀 ID로 변환하는 헬퍼 함수
  const getTeamId = (teamProjectId: string): number => {
    // 실제 백엔드에 존재하는 팀 ID로 매핑
    // 현재 백엔드에 teamId: 2가 없어서 모든 팀을 teamId: 1로 매핑
    console.log("🏷️ Team mapping for schedule:", {
      teamProjectId,
      mappedTeamId: 1,
    });
    return 1; // 임시로 모든 팀을 1번 팀으로 매핑

    // 나중에 백엔드에 팀이 추가되면 아래 매핑을 사용
    // switch (teamProjectId) {
    //   case "tp_alpha":
    //     return 1;
    //   case "tp_beta":
    //     return 2;
    //   case "tp_personal":
    //     return 3;
    //   case "tp_gongji_features":
    //     return 4;
    //   default:
    //     return 1;
    // }
  };

  // 스케줄 데이터 로드
  const loadSchedules = useCallback(async () => {
    try {
      setLoading(true);
      // teamProjectId에서 숫자 추출 (tp_alpha -> 1, tp_beta -> 2 등)
      const teamId = getTeamId(teamProjectId);

      // 현재 월의 시작일과 종료일 계산
      const startDate = new Date(year, month, 1).toISOString();
      const endDate = new Date(year, month + 1, 0, 23, 59, 59).toISOString();

      console.log("🗓️ Loading schedules for team calendar:", {
        teamProjectId,
        teamId,
        year,
        month,
        startDate,
        endDate,
      });

      const schedules = await scheduleApi.getSchedulesByDateRange(
        teamId,
        startDate,
        endDate
      );

      console.log("📋 Received schedules:", schedules);

      const calendarEvents: CalendarEvent[] = schedules.map(
        (schedule: ScheduleResponse) => {
          // 백엔드 타입을 프론트엔드 타입으로 매핑
          const mapScheduleType = (
            backendType: string
          ): CalendarEvent["type"] => {
            switch (backendType.toLowerCase()) {
              case "meeting":
                return "meeting";
              case "deadline":
                return "deadline";
              case "workshop":
                return "workshop";
              case "vacation":
                return "vacation";
              default:
                return "other";
            }
          };

          return {
            id: schedule.id.toString(),
            title: schedule.title,
            start: new Date(schedule.startDate),
            end: new Date(schedule.endDate),
            description: schedule.scheduleDesc || "",
            type: mapScheduleType(schedule.type),
            teamProjectId: teamProjectId,
          };
        }
      );

      console.log("📅 Mapped calendar events:", calendarEvents);
      setEvents(calendarEvents);
    } catch (error) {
      console.error("❌ 스케줄 로드 실패:", error);
      setEvents([]);
    } finally {
      setLoading(false);
    }
  }, [teamProjectId, year, month]);

  useEffect(() => {
    loadSchedules();
    resetModalState(new Date(currentDisplayDate));
  }, [loadSchedules, currentDisplayDate]);

  const resetModalState = (dateForNewEvent?: Date) => {
    setEditingEventId(null);
    setNewEvent({
      title: "",
      start: dateForNewEvent || new Date(),
      end: dateForNewEvent || new Date(),
      type: "meeting",
      teamProjectId,
    });
  };

  const handleAddEvent = async () => {
    if (newEvent.title && newEvent.start && newEvent.end) {
      try {
        const teamId = getTeamId(teamProjectId);
        const accountId = parseInt(currentUser.id) || 1;

        const scheduleData: ScheduleCreateRequest = {
          title: newEvent.title,
          startDate: newEvent.start.toISOString(),
          endDate: newEvent.end.toISOString(),
          scheduleDesc: newEvent.description || "",
          type: newEvent.type?.toUpperCase() || "MEETING",
        };

        await scheduleApi.createSchedule(teamId, accountId, scheduleData);
        await loadSchedules(); // 새로 생성된 스케줄을 다시 로드
        setShowAddEventModal(false);
        resetModalState(new Date(currentDisplayDate));
      } catch (error) {
        console.error("스케줄 생성 실패:", error);
        alert("스케줄 생성에 실패했습니다.");
      }
    }
  };

  const handleUpdateEvent = async () => {
    if (editingEventId && newEvent.title && newEvent.start && newEvent.end) {
      try {
        const teamId = getTeamId(teamProjectId);
        const accountId = parseInt(currentUser.id) || 1;

        const scheduleData: ScheduleUpdateRequest = {
          title: newEvent.title,
          startDate: newEvent.start.toISOString(),
          endDate: newEvent.end.toISOString(),
          scheduleDesc: newEvent.description || "",
          type: newEvent.type?.toUpperCase() || "MEETING",
        };

        await scheduleApi.updateSchedule(
          teamId,
          parseInt(editingEventId),
          accountId,
          scheduleData
        );
        await loadSchedules(); // 수정된 스케줄을 다시 로드
        setShowAddEventModal(false);
        resetModalState(new Date(currentDisplayDate));
      } catch (error) {
        console.error("스케줄 수정 실패:", error);
        alert("스케줄 수정에 실패했습니다.");
      }
    }
  };

  const handleDeleteEvent = async () => {
    if (editingEventId) {
      if (window.confirm("이 일정을 정말 삭제하시겠습니까?")) {
        try {
          const teamId = getTeamId(teamProjectId);
          const accountId = parseInt(currentUser.id) || 1;

          await scheduleApi.deleteSchedule(
            teamId,
            parseInt(editingEventId),
            accountId
          );
          await loadSchedules(); // 삭제 후 다시 로드
          setShowAddEventModal(false);
          resetModalState(new Date(currentDisplayDate));
        } catch (error) {
          console.error("스케줄 삭제 실패:", error);
          alert("스케줄 삭제에 실패했습니다.");
        }
      }
    }
  };

  const prevMonth = () => {
    setCurrentDisplayDate(new Date(year, month - 1, 1));
  };

  const nextMonth = () => {
    setCurrentDisplayDate(new Date(year, month + 1, 1));
  };

  const handleDayClick = (day: number) => {
    const clickedDate = new Date(year, month, day);
    resetModalState(clickedDate);
    setShowAddEventModal(true);
  };

  const handleEventClick = (
    eventToEdit: CalendarEvent,
    e: React.MouseEvent
  ) => {
    e.stopPropagation();
    setEditingEventId(eventToEdit.id);
    setNewEvent({
      title: eventToEdit.title,
      start: new Date(eventToEdit.start),
      end: new Date(eventToEdit.end),
      description: eventToEdit.description || "",
      type: eventToEdit.type,
      teamProjectId: eventToEdit.teamProjectId,
    });
    setShowAddEventModal(true);
  };

  const numDaysInMonth = getDaysInMonth(year, month);
  const firstDayOfMonthIndex = getFirstDayOfMonth(year, month);

  const calendarCells = [];
  for (let i = 0; i < firstDayOfMonthIndex; i++) {
    calendarCells.push(
      <div
        key={`empty-${i}`}
        className="border border-neutral-200 bg-neutral-50 h-32 sm:h-36"
      ></div>
    );
  }

  for (let day = 1; day <= numDaysInMonth; day++) {
    const currentDateObj = new Date(year, month, day);
    currentDateObj.setHours(0, 0, 0, 0);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const isToday = currentDateObj.getTime() === today.getTime();

    const dayEvents = events.filter((event) => {
      const eventStart = new Date(event.start);
      eventStart.setHours(0, 0, 0, 0);
      const eventEnd = new Date(event.end);
      eventEnd.setHours(23, 59, 59, 999);
      return currentDateObj >= eventStart && currentDateObj <= eventEnd;
    });

    calendarCells.push(
      <div
        key={day}
        className={`border border-neutral-200 p-1.5 sm:p-2 h-32 sm:h-36 relative flex flex-col group hover:bg-primary-light/5 cursor-pointer transition-colors duration-150`}
        onClick={() => handleDayClick(day)}
        role="button"
        tabIndex={0}
        aria-label={`Date ${day}, ${dayEvents.length} events`}
      >
        <span
          className={`text-xs sm:text-sm font-medium ${
            isToday
              ? "bg-primary text-white rounded-full w-6 h-6 flex items-center justify-center"
              : "text-neutral-700"
          }`}
        >
          {day}
        </span>
        <div className="mt-1 flex-grow overflow-y-auto space-y-0.5 max-h-[100px] sm:max-h-[110px] pr-1 scrollbar-thin">
          {dayEvents.slice(0, 3).map((event) => (
            <div
              key={event.id}
              className={`text-[10px] sm:text-xs p-1 rounded-sm truncate text-white cursor-pointer ${
                event.type === "deadline"
                  ? "bg-red-500 hover:bg-red-600"
                  : event.type === "meeting"
                  ? "bg-blue-500 hover:bg-blue-600"
                  : event.type === "workshop"
                  ? "bg-purple-500 hover:bg-purple-600"
                  : event.type === "vacation"
                  ? "bg-orange-500 hover:bg-orange-600"
                  : "bg-green-500 hover:bg-green-600"
              }`}
              title={event.title}
              onClick={(e) => handleEventClick(event, e)}
            >
              {event.title}
            </div>
          ))}
          {dayEvents.length > 3 && (
            <div className="text-[10px] text-neutral-500 text-center mt-0.5">
              +{dayEvents.length - 3} more
            </div>
          )}
        </div>
      </div>
    );
  }

  const handleModalClose = () => {
    setShowAddEventModal(false);
    resetModalState(new Date(currentDisplayDate));
  };

  return (
    <Card
      title="📅 팀 공유 캘린더"
      actions={
        <Button
          size="sm"
          onClick={() => {
            resetModalState(new Date(currentDisplayDate));
            setShowAddEventModal(true);
          }}
          leftIcon={<PlusCircleIcon />}
        >
          일정 추가
        </Button>
      }
    >
      <div className="mb-4 flex justify-between items-center">
        <Button
          variant="outline"
          size="sm"
          onClick={prevMonth}
          aria-label="Previous month"
        >
          <ChevronLeftIcon className="w-5 h-5" />
        </Button>
        <h3 className="text-lg sm:text-xl font-semibold text-neutral-800">
          {year}년 {month + 1}월
        </h3>
        <Button
          variant="outline"
          size="sm"
          onClick={nextMonth}
          aria-label="Next month"
        >
          <ChevronRightIcon className="w-5 h-5" />
        </Button>
      </div>

      {loading ? (
        <p className="text-center text-neutral-500 py-8">
          스케줄을 불러오는 중...
        </p>
      ) : (
        <div className="grid grid-cols-7 gap-px border-t border-l border-neutral-200 bg-neutral-200">
          {daysOfWeek.map((day) => (
            <div
              key={day}
              className="text-center py-2 text-xs sm:text-sm font-medium text-neutral-600 bg-neutral-100 border-r border-b border-neutral-200"
            >
              {day}
            </div>
          ))}
          {calendarCells}
        </div>
      )}

      <Modal
        isOpen={showAddEventModal}
        onClose={handleModalClose}
        title={editingEventId ? "일정 수정" : "새 일정 등록"}
        footer={
          <div className="flex justify-between w-full">
            <div>
              {editingEventId && (
                <Button
                  variant="danger"
                  onClick={handleDeleteEvent}
                  leftIcon={<TrashIcon className="w-4 h-4" />}
                >
                  삭제
                </Button>
              )}
            </div>
            <div className="space-x-2">
              <Button variant="ghost" onClick={handleModalClose}>
                취소
              </Button>
              <Button
                onClick={editingEventId ? handleUpdateEvent : handleAddEvent}
              >
                {editingEventId ? "변경사항 저장" : "등록"}
              </Button>
            </div>
          </div>
        }
      >
        <div className="space-y-4">
          <Input
            label="일정 제목"
            value={newEvent.title || ""}
            onChange={(e) =>
              setNewEvent((prev) => ({ ...prev, title: e.target.value }))
            }
            required
          />
          <Input
            label="시작일"
            type="datetime-local"
            value={
              newEvent.start
                ? new Date(
                    newEvent.start.getTime() -
                      newEvent.start.getTimezoneOffset() * 60000
                  )
                    .toISOString()
                    .slice(0, 16)
                : ""
            }
            onChange={(e) =>
              setNewEvent((prev) => ({
                ...prev,
                start: new Date(e.target.value),
              }))
            }
            required
          />
          <Input
            label="종료일"
            type="datetime-local"
            value={
              newEvent.end
                ? new Date(
                    newEvent.end.getTime() -
                      newEvent.end.getTimezoneOffset() * 60000
                  )
                    .toISOString()
                    .slice(0, 16)
                : ""
            }
            onChange={(e) =>
              setNewEvent((prev) => ({
                ...prev,
                end: new Date(e.target.value),
              }))
            }
            required
          />
          <TextArea
            label="설명 (선택 사항)"
            value={newEvent.description || ""}
            onChange={(e) =>
              setNewEvent((prev) => ({ ...prev, description: e.target.value }))
            }
            rows={3}
          />
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-1">
              종류
            </label>
            <select
              value={newEvent.type || "meeting"}
              onChange={(e) =>
                setNewEvent((prev) => ({
                  ...prev,
                  type: e.target.value as CalendarEvent["type"],
                }))
              }
              className="block w-full px-3 py-2 border border-neutral-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
            >
              <option value="meeting">회의</option>
              <option value="deadline">마감일</option>
              <option value="workshop">워크샵</option>
              <option value="vacation">휴가</option>
              <option value="other">기타</option>
            </select>
          </div>
        </div>
      </Modal>
    </Card>
  );
};

interface KanbanCardDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  card: KanbanCardType | null;
  columnTitle: string;
  onUpdateCard: (updatedCard: KanbanCardType) => void;
  onAddComment: (cardId: string, commentText: string) => void;
  onApproveCard: (cardId: string) => void;
  currentUser: User;
}

const KanbanCardDetailModal: React.FC<KanbanCardDetailModalProps> = ({
  isOpen,
  onClose,
  card,
  columnTitle,
  onUpdateCard,
  onAddComment,
  onApproveCard,
  currentUser,
}) => {
  const [editedTitle, setEditedTitle] = useState("");
  const [editedDescription, setEditedDescription] = useState("");
  const [newComment, setNewComment] = useState("");

  useEffect(() => {
    if (card) {
      setEditedTitle(card.title);
      setEditedDescription(card.description || "");
    }
  }, [card]);

  if (!isOpen || !card) return null;

  const handleSave = () => {
    onUpdateCard({
      ...card,
      title: editedTitle,
      description: editedDescription,
    });
  };

  const handleAddComment = () => {
    if (newComment.trim()) {
      onAddComment(card.id, newComment.trim());
      setNewComment("");
    }
  };

  const isDoneColumn = columnTitle === "Done";

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="작업 상세 정보"
      footer={
        <div className="flex justify-between w-full">
          <div>
            {isDoneColumn &&
              (card.isApproved ? (
                <Button
                  size="sm"
                  variant="ghost"
                  disabled
                  className="text-green-500"
                  leftIcon={<CheckCircleIcon className="w-5 h-5" />}
                >
                  승인 완료
                </Button>
              ) : (
                <Button
                  size="sm"
                  variant="primary"
                  onClick={() => onApproveCard(card.id)}
                  leftIcon={<CheckCircleIcon className="w-5 h-5" />}
                >
                  승인 요청
                </Button>
              ))}
          </div>
          <div className="space-x-2">
            <Button variant="ghost" onClick={onClose}>
              닫기
            </Button>
            <Button onClick={handleSave}>변경사항 저장</Button>
          </div>
        </div>
      }
    >
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700">
            제목
          </label>
          <Input
            value={editedTitle}
            onChange={(e) => setEditedTitle(e.target.value)}
            className="text-lg font-semibold"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700">
            설명
          </label>
          <TextArea
            value={editedDescription}
            onChange={(e) => setEditedDescription(e.target.value)}
            rows={4}
          />
        </div>

        <div className="text-sm">
          <p>
            <span className="font-medium">상태:</span> {columnTitle}
          </p>
          {card.dueDate && (
            <p>
              <span className="font-medium">마감일:</span>{" "}
              {new Date(card.dueDate).toLocaleDateString()}
            </p>
          )}
          {card.assigneeIds && card.assigneeIds.length > 0 && (
            <p>
              <span className="font-medium">담당자:</span>{" "}
              {card.assigneeIds.join(", ")} (ID)
            </p>
          )}
        </div>

        <div>
          <h4 className="text-md font-semibold text-neutral-700 mb-2 border-t pt-3">
            댓글
          </h4>
          <div className="space-y-2 max-h-40 overflow-y-auto mb-3 bg-neutral-50 p-2 rounded">
            {card.comments && card.comments.length > 0 ? (
              card.comments.map((comment) => (
                <div
                  key={comment.id}
                  className="text-xs p-1.5 bg-white rounded shadow-sm"
                >
                  <p className="text-neutral-800">{comment.text}</p>
                  <p className="text-neutral-500 mt-0.5">
                    - {comment.userName} (
                    {new Date(comment.createdAt).toLocaleString()})
                  </p>
                </div>
              ))
            ) : (
              <p className="text-xs text-neutral-500 italic">
                댓글이 없습니다.
              </p>
            )}
          </div>
          <div className="flex items-start space-x-2">
            <TextArea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="댓글을 입력하세요..."
              rows={2}
              className="flex-grow"
            />
            <Button
              onClick={handleAddComment}
              size="sm"
              variant="outline"
              className="h-full"
            >
              댓글 추가
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  );
};

const TeamKanbanBoard: React.FC<{
  teamProjectId: string;
  currentUser: User;
}> = ({ teamProjectId, currentUser }) => {
  const [board, setBoard] = useState<KanbanBoard | null>(null);
  const [selectedCard, setSelectedCard] = useState<KanbanCardType | null>(null);
  const [isCardDetailModalOpen, setIsCardDetailModalOpen] = useState(false);
  const [selectedCardColumnTitle, setSelectedCardColumnTitle] = useState("");

  useEffect(() => {
    const mockCommentsUser1: KanbanComment[] = [
      {
        id: "comment1-1",
        cardId: "card1",
        userId: currentUser.id,
        userName: currentUser.name || "김코딩",
        text: "이거 오늘까지 마무리 가능할까요?",
        createdAt: new Date(Date.now() - 3600000),
      },
      {
        id: "comment1-2",
        cardId: "card1",
        userId: "otherUser",
        userName: "박해커",
        text: "네, 거의 다 됐습니다!",
        createdAt: new Date(Date.now() - 1800000),
      },
    ];
    const mockCommentsUser3: KanbanComment[] = [
      {
        id: "comment3-1",
        cardId: "card3",
        userId: currentUser.id,
        userName: currentUser.name || "김코딩",
        text: "OAuth 부분에서 이슈가 있는데 같이 봐주실 수 있나요?",
        createdAt: new Date(Date.now() - 7200000),
      },
    ];

    const mockCards: KanbanCardType[] = [
      {
        id: "card1",
        columnId: "col1",
        title: "디자인 시안 작업",
        description:
          "메인 페이지 및 주요 서브 페이지 UI/UX 디자인 작업. 사용성 테스트 포함.",
        order: 0,
        assigneeIds: [currentUser.id],
        comments: mockCommentsUser1,
        dueDate: new Date(Date.now() + 2 * 86400000),
      },
      {
        id: "card2",
        columnId: "col1",
        title: "API 명세서 작성",
        description:
          "사용자 인증, 팀 관리, 게시판 CRUD 관련 API 상세 명세 작성",
        order: 1,
        assigneeIds: ["dev_lead_id"],
      },
      {
        id: "card3",
        columnId: "col2",
        title: "로그인 기능 개발",
        description:
          "OAuth 2.0 (Google, Kakao) 연동 및 자체 이메일/비밀번호 로그인 기능 구현. JWT 토큰 기반 인증.",
        order: 0,
        dueDate: new Date(Date.now() + 5 * 86400000),
        comments: mockCommentsUser3,
        assigneeIds: [currentUser.id, "backend_dev_id"],
      },
      {
        id: "card4",
        columnId: "col3",
        title: "1차 QA 완료",
        description:
          "회원가입 및 로그인 플로우, 기본 팀 생성 기능에 대한 QA 완료됨.",
        order: 0,
        isApproved: true,
      },
      {
        id: "card5",
        columnId: "col3",
        title: "팀 공지사항 UI 개발",
        description: "팀 스페이스 내 공지사항 CRUD UI 개발 완료.",
        order: 1,
        isApproved: false,
      },
    ];
    setBoard({
      id: `kanban-${teamProjectId}`,
      teamProjectId,
      columns: [
        {
          id: "col1",
          boardId: `kanban-${teamProjectId}`,
          title: "To Do",
          cards: mockCards
            .filter((c) => c.columnId === "col1")
            .sort((a, b) => a.order - b.order),
          order: 0,
        },
        {
          id: "col2",
          boardId: `kanban-${teamProjectId}`,
          title: "In Progress",
          cards: mockCards
            .filter((c) => c.columnId === "col2")
            .sort((a, b) => a.order - b.order),
          order: 1,
        },
        {
          id: "col3",
          boardId: `kanban-${teamProjectId}`,
          title: "Done",
          cards: mockCards
            .filter((c) => c.columnId === "col3")
            .sort((a, b) => a.order - b.order),
          order: 2,
        },
      ],
    });
  }, [teamProjectId, currentUser.id, currentUser.name]);

  const handleCardClick = (card: KanbanCardType, columnTitle: string) => {
    setSelectedCard(card);
    setSelectedCardColumnTitle(columnTitle);
    setIsCardDetailModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsCardDetailModalOpen(false);
    setSelectedCard(null);
    setSelectedCardColumnTitle("");
  };

  const handleUpdateCard = (updatedCard: KanbanCardType) => {
    setBoard((prevBoard) => {
      if (!prevBoard) return null;
      return {
        ...prevBoard,
        columns: prevBoard.columns.map((col) => ({
          ...col,
          cards: col.cards.map((card) =>
            card.id === updatedCard.id ? updatedCard : card
          ),
        })),
      };
    });
    setSelectedCard(updatedCard);
  };

  const handleAddCommentToCard = (cardId: string, commentText: string) => {
    const newComment: KanbanComment = {
      id: `comment-${Date.now()}`,
      cardId,
      userId: currentUser.id,
      userName: currentUser.name || "Current User",
      text: commentText,
      createdAt: new Date(),
    };
    setBoard((prevBoard) => {
      if (!prevBoard) return null;
      const newColumns = prevBoard.columns.map((col) => ({
        ...col,
        cards: col.cards.map((card) => {
          if (card.id === cardId) {
            return {
              ...card,
              comments: [...(card.comments || []), newComment],
            };
          }
          return card;
        }),
      }));
      const updatedCardFromState = newColumns
        .flatMap((col) => col.cards)
        .find((c) => c.id === cardId);
      if (updatedCardFromState) setSelectedCard(updatedCardFromState);
      return { ...prevBoard, columns: newColumns };
    });
  };

  const handleApproveCard = (cardId: string) => {
    setBoard((prevBoard) => {
      if (!prevBoard) return null;
      const newColumns = prevBoard.columns.map((col) => ({
        ...col,
        cards: col.cards.map((card) => {
          if (card.id === cardId) {
            return { ...card, isApproved: true };
          }
          return card;
        }),
      }));
      const updatedCardFromState = newColumns
        .flatMap((col) => col.cards)
        .find((c) => c.id === cardId);
      if (updatedCardFromState) setSelectedCard(updatedCardFromState);
      return { ...prevBoard, columns: newColumns };
    });
  };

  if (!board)
    return (
      <Card title="📊 칸반 보드">
        <p>로딩 중...</p>
      </Card>
    );

  return (
    <Card
      title="📊 칸반 보드"
      actions={
        <Button size="sm" leftIcon={<PlusCircleIcon />}>
          새 작업 추가
        </Button>
      }
    >
      <div className="flex space-x-4 overflow-x-auto p-2 bg-neutral-50 rounded min-h-[500px]">
        {board.columns
          .sort((a, b) => a.order - b.order)
          .map((column) => (
            <div
              key={column.id}
              className="w-80 bg-neutral-100 p-3 rounded-lg shadow-sm flex-shrink-0"
            >
              <h3 className="font-semibold text-neutral-700 mb-3 px-1">
                {column.title} ({column.cards.length})
              </h3>
              <div className="space-y-3 min-h-[450px]">
                {column.cards.map((card) => (
                  <div
                    key={card.id}
                    className="bg-white p-3 rounded-md shadow border border-neutral-200 hover:shadow-lg hover:border-primary-light transition-all cursor-pointer group"
                    onClick={() => handleCardClick(card, column.title)}
                  >
                    <h4 className="font-medium text-sm text-neutral-800 group-hover:text-primary">
                      {card.title}
                    </h4>
                    {card.description && (
                      <p className="text-xs text-neutral-600 mt-1 truncate group-hover:whitespace-normal">
                        {card.description}
                      </p>
                    )}
                    {card.dueDate && (
                      <p
                        className={`text-xs mt-1.5 ${
                          new Date(card.dueDate) < new Date() &&
                          column.id !== "col3"
                            ? "text-red-600 font-medium"
                            : "text-neutral-500"
                        }`}
                      >
                        마감: {new Date(card.dueDate).toLocaleDateString()}
                      </p>
                    )}

                    <div className="mt-2 pt-2 border-t border-neutral-100 flex justify-between items-center">
                      <div className="flex -space-x-1 overflow-hidden">
                        {card.assigneeIds &&
                          card.assigneeIds
                            .slice(0, 3)
                            .map((assigneeId) => (
                              <img
                                key={assigneeId}
                                className="inline-block h-5 w-5 rounded-full ring-1 ring-white"
                                src={`https://picsum.photos/seed/${assigneeId}/20/20`}
                                alt={`Assignee ${assigneeId}`}
                                title={assigneeId}
                              />
                            ))}
                        {card.assigneeIds && card.assigneeIds.length > 3 && (
                          <span className="text-xs text-neutral-400 self-center pl-1">
                            +{card.assigneeIds.length - 3}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center space-x-2">
                        {card.comments && card.comments.length > 0 && (
                          <span className="text-xs text-neutral-500 flex items-center">
                            <ChatBubbleBottomCenterTextIcon className="w-3.5 h-3.5 mr-0.5" />
                            {card.comments.length}
                          </span>
                        )}
                        {card.isApproved && column.id === "col3" && (
                          <CheckCircleIcon
                            className="w-4 h-4 text-green-500"
                            title="승인 완료"
                          />
                        )}
                      </div>
                    </div>
                  </div>
                ))}
                {column.cards.length === 0 && (
                  <p className="text-xs text-neutral-400 p-2 text-center">
                    이 컬럼에 카드가 없습니다.
                  </p>
                )}
              </div>
            </div>
          ))}
      </div>
      {selectedCard && (
        <KanbanCardDetailModal
          isOpen={isCardDetailModalOpen}
          onClose={handleCloseModal}
          card={selectedCard}
          columnTitle={selectedCardColumnTitle}
          onUpdateCard={handleUpdateCard}
          onAddComment={handleAddCommentToCard}
          onApproveCard={handleApproveCard}
          currentUser={currentUser}
        />
      )}
    </Card>
  );
};

interface BulletinPostDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  post: BulletinPost | null;
  onAddComment: (postId: string, commentText: string) => void;
  onDeletePost: (postId: string) => void;
  currentUser: User;
}

const BulletinPostDetailModal: React.FC<BulletinPostDetailModalProps> = ({
  isOpen,
  onClose,
  post,
  onAddComment,
  onDeletePost,
  currentUser,
}) => {
  const [newCommentText, setNewCommentText] = useState("");

  if (!isOpen || !post) return null;

  const handleAddComment = () => {
    if (newCommentText.trim()) {
      onAddComment(post.id, newCommentText.trim());
      setNewCommentText("");
    }
  };

  const handleDelete = () => {
    if (window.confirm("이 게시글을 정말 삭제하시겠습니까?")) {
      onDeletePost(post.id);
      onClose();
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={post.title}
      footer={
        <div className="flex justify-between w-full">
          {currentUser.id === post.authorId && (
            <Button variant="danger" onClick={handleDelete} size="sm">
              게시글 삭제
            </Button>
          )}
          <Button variant="ghost" onClick={onClose} className="ml-auto">
            닫기
          </Button>
        </div>
      }
    >
      <div className="space-y-4 max-h-[70vh] overflow-y-auto pr-2">
        <div className="pb-2 border-b">
          <p className="text-xs text-neutral-500">
            작성자: {post.authorName} | 작성일:{" "}
            {new Date(post.createdAt).toLocaleString()}
          </p>
        </div>
        <div className="prose prose-sm max-w-none text-neutral-700 whitespace-pre-line">
          {post.content}
        </div>

        {post.attachments && post.attachments.length > 0 && (
          <div>
            <h5 className="text-sm font-semibold text-neutral-600 mt-3 mb-1">
              첨부파일
            </h5>
            <ul className="list-disc list-inside text-xs">
              {post.attachments.map((att) => (
                <li key={att.id}>
                  <a
                    href={fileApi.getDownloadUrl(att.id)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline"
                    download={att.fileName}
                  >
                    {att.fileName}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="pt-4 border-t">
          <h4 className="text-md font-semibold text-neutral-700 mb-2">
            댓글 ({post.comments?.length || 0})
          </h4>
          <div className="space-y-3 mb-3">
            {post.comments && post.comments.length > 0 ? (
              post.comments.map((comment) => (
                <div
                  key={comment.id}
                  className="text-xs p-2 bg-neutral-100 rounded"
                >
                  <p className="text-neutral-800 whitespace-pre-line">
                    {comment.text}
                  </p>
                  <p className="text-neutral-500 mt-1">
                    - {comment.userName} (
                    {new Date(comment.createdAt).toLocaleString()})
                  </p>
                </div>
              ))
            ) : (
              <p className="text-xs text-neutral-500 italic">
                댓글이 없습니다.
              </p>
            )}
          </div>
          <div className="flex items-start space-x-2">
            <TextArea
              value={newCommentText}
              onChange={(e) => setNewCommentText(e.target.value)}
              placeholder="댓글을 입력하세요..."
              rows={2}
              className="flex-grow text-sm"
            />
            <Button
              onClick={handleAddComment}
              size="sm"
              variant="outline"
              className="h-full"
            >
              댓글 작성
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  );
};

const TeamBulletinBoard: React.FC<{
  teamProjectId: string;
  currentUser: User;
}> = ({ teamProjectId, currentUser }) => {
  const [posts, setPosts] = useState<BulletinPost[]>([]);
  const [isCreatePostModalOpen, setIsCreatePostModalOpen] = useState(false);
  const [newPostData, setNewPostData] = useState<{
    title: string;
    content: string;
  }>({ title: "", content: "" });
  const [selectedPost, setSelectedPost] = useState<BulletinPost | null>(null);
  const [isPostDetailModalOpen, setIsPostDetailModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [uploading, setUploading] = useState(false);

  // 팀 프로젝트 ID를 백엔드 팀 ID로 변환하는 헬퍼 함수
  const getTeamId = (teamProjectId: string): number => {
    switch (teamProjectId) {
      case "tp_alpha":
        return 1;
      case "tp_beta":
        return 2;
      case "tp_personal":
        return 3;
      case "tp_gongji_features":
        return 4;
      default:
        return 1;
    }
  };

  // 게시글 로드 (게시판 전용)
  const loadPosts = useCallback(async () => {
    try {
      setLoading(true);
      // 게시판 보드 ID는 1 (공지사항과 분리)
      const teamId = getTeamId(teamProjectId);
      const response = await postApi.getPosts(teamId, 1, 0, 50);
      const bulletinPosts: BulletinPost[] = await Promise.all(
        response.content.map(async (post: PostResponse) => {
          // 각 게시글의 댓글도 함께 로드
          let comments: BulletinComment[] = [];
          try {
            const commentsResponse = await commentApi.getComments(
              post.id,
              0,
              100
            );
            comments = commentsResponse.content.map(
              (comment: CommentResponse) => ({
                id: comment.id.toString(),
                postId: post.id.toString(),
                userId: comment.authorId.toString(),
                userName: comment.authorName,
                text: comment.content,
                createdAt: new Date(comment.createdAt),
              })
            );
          } catch (error) {
            console.error(`댓글 로드 실패 (게시글 ${post.id}):`, error);
          }

          return {
            id: post.id.toString(),
            teamProjectId: teamProjectId,
            title: post.title,
            content: post.content,
            authorId: post.authorId.toString(),
            authorName: post.authorName,
            createdAt: new Date(post.createdAt),
            updatedAt: post.updatedAt ? new Date(post.updatedAt) : undefined,
            attachments:
              post.attachments?.map((att) => ({
                id: att.id.toString(),
                postId: post.id.toString(),
                fileName: att.originalName || `첨부파일_${att.id}`, // originalName이 없을 경우 기본 이름 제공
                fileUrl: `/files/${att.id}/download`, // 다운로드 URL 수정
              })) || [],
            comments: comments,
          };
        })
      );
      setPosts(bulletinPosts);
    } catch (error) {
      console.error("게시글 로드 실패:", error);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [teamProjectId]);

  useEffect(() => {
    loadPosts();
  }, [loadPosts]);

  const handleOpenCreatePostModal = () => {
    setNewPostData({ title: "", content: "" });
    setSelectedFiles([]);
    setIsCreatePostModalOpen(true);
  };

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (files) {
      setSelectedFiles(Array.from(files));
    }
  };

  const handleRemoveFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleCreatePost = async () => {
    if (!newPostData.title.trim() || !newPostData.content.trim()) {
      alert("제목과 내용을 모두 입력해주세요.");
      return;
    }

    try {
      setUploading(true);
      const accountId = parseInt(currentUser.id) || 1;

      if (selectedFiles.length > 0) {
        // 파일이 있는 경우 새로운 방식으로 처리
        const postData: PostCreateRequest = {
          title: newPostData.title,
          content: newPostData.content,
          boardId: 1, // 게시판용 보드 ID (공지사항과 분리)
        };

        const teamId = getTeamId(teamProjectId);
        // 새로운 fileApi.createPostWithAttachments 사용
        await fileApi.createPostWithAttachments(
          teamId,
          accountId,
          postData,
          selectedFiles
        );
      } else {
        // 파일이 없는 경우 일반 게시글 생성
        const postData: PostCreateRequest = {
          title: newPostData.title,
          content: newPostData.content,
          boardId: 1, // 게시판용 보드 ID (공지사항과 분리)
        };

        const teamId = getTeamId(teamProjectId);
        await postApi.createPost(teamId, accountId, postData);
      }

      // 새로 생성된 게시글을 다시 로드
      await loadPosts();
      setIsCreatePostModalOpen(false);
      setNewPostData({ title: "", content: "" });
      setSelectedFiles([]);
    } catch (error) {
      console.error("게시글 생성 실패:", error);
      alert("게시글 생성에 실패했습니다.");
    } finally {
      setUploading(false);
    }
  };

  const handleOpenPostDetail = (post: BulletinPost) => {
    setSelectedPost(post);
    setIsPostDetailModalOpen(true);
  };

  const handleAddBulletinComment = async (
    postId: string,
    commentText: string
  ) => {
    try {
      const commentData: CommentCreateRequest = {
        content: commentText,
      };

      const accountId = parseInt(currentUser.id) || 1;
      await commentApi.createComment(parseInt(postId), accountId, commentData);

      // 댓글 추가 후 게시글 다시 로드
      await loadPosts();

      // 선택된 게시글 업데이트
      if (selectedPost && selectedPost.id === postId) {
        const updatedPost = posts.find((p) => p.id === postId);
        if (updatedPost) {
          setSelectedPost(updatedPost);
        }
      }
    } catch (error) {
      console.error("댓글 추가 실패:", error);
      alert("댓글 추가에 실패했습니다.");
    }
  };

  const handleDeleteBulletinPost = async (postId: string) => {
    try {
      const accountId = parseInt(currentUser.id) || 1;
      await postApi.deletePost(parseInt(postId), accountId);

      // 삭제 후 게시글 다시 로드
      await loadPosts();

      if (selectedPost && selectedPost.id === postId) {
        setIsPostDetailModalOpen(false);
        setSelectedPost(null);
      }
    } catch (error) {
      console.error("게시글 삭제 실패:", error);
      alert("게시글 삭제에 실패했습니다.");
    }
  };

  return (
    <Card
      title="📋 게시판"
      actions={
        <Button
          size="sm"
          onClick={handleOpenCreatePostModal}
          leftIcon={<PlusCircleIcon />}
        >
          새 글 작성
        </Button>
      }
    >
      {loading ? (
        <p className="text-neutral-500 py-4 text-center">
          게시글을 불러오는 중...
        </p>
      ) : (
        <ul className="space-y-3">
          {posts.map((post) => (
            <li
              key={post.id}
              className="p-3 bg-neutral-50 rounded shadow-sm hover:shadow-md transition-shadow group"
            >
              <div className="flex justify-between items-start">
                <div
                  className="flex-grow cursor-pointer"
                  onClick={() => handleOpenPostDetail(post)}
                >
                  <h4 className="font-semibold text-primary-dark hover:underline">
                    {post.title}
                  </h4>
                  <p className="text-xs text-neutral-600 truncate max-w-md">
                    {post.content}
                  </p>
                  <p className="text-xs text-neutral-500 mt-1">
                    작성자: {post.authorName} |{" "}
                    {new Date(post.createdAt).toLocaleDateString()} | 댓글:{" "}
                    {post.comments?.length || 0}
                  </p>
                </div>
                {currentUser.id === post.authorId && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      if (
                        window.confirm("이 게시글을 정말 삭제하시겠습니까?")
                      ) {
                        handleDeleteBulletinPost(post.id);
                      }
                    }}
                    className="opacity-0 group-hover:opacity-100 transition-opacity"
                    aria-label="게시글 삭제"
                  >
                    <TrashIcon className="w-4 h-4 text-red-500" />
                  </Button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      {!loading && posts.length === 0 && (
        <p className="text-neutral-500 py-4 text-center">게시글이 없습니다.</p>
      )}

      <Modal
        isOpen={isCreatePostModalOpen}
        onClose={() => setIsCreatePostModalOpen(false)}
        title="새 게시글 작성"
        footer={
          <div className="flex justify-end space-x-2">
            <Button
              variant="ghost"
              onClick={() => setIsCreatePostModalOpen(false)}
              disabled={uploading}
            >
              취소
            </Button>
            <Button onClick={handleCreatePost} disabled={uploading}>
              {uploading ? "등록 중..." : "등록"}
            </Button>
          </div>
        }
      >
        <div className="space-y-3">
          <Input
            label="제목"
            value={newPostData.title}
            onChange={(e) =>
              setNewPostData((prev) => ({ ...prev, title: e.target.value }))
            }
            required
          />
          <TextArea
            label="내용"
            value={newPostData.content}
            onChange={(e) =>
              setNewPostData((prev) => ({ ...prev, content: e.target.value }))
            }
            rows={8}
            required
          />
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-1">
              첨부파일 (선택)
            </label>
            <Input
              type="file"
              multiple
              className="text-sm"
              onChange={handleFileSelect}
            />
            {selectedFiles.length > 0 && (
              <div className="mt-2">
                <span className="text-sm font-medium text-neutral-700">
                  선택된 파일:
                </span>
                <ul className="list-disc list-inside text-xs mt-1">
                  {selectedFiles.map((file, index) => (
                    <li
                      key={index}
                      className="flex justify-between items-center"
                    >
                      <span className="text-neutral-700">{file.name}</span>
                      <Button
                        variant="ghost"
                        size="xs"
                        onClick={() => handleRemoveFile(index)}
                        className="opacity-70 hover:opacity-100"
                        aria-label="파일 제거"
                      >
                        <TrashIcon className="w-4 h-4 text-red-500" />
                      </Button>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      </Modal>

      {selectedPost && (
        <BulletinPostDetailModal
          isOpen={isPostDetailModalOpen}
          onClose={() => setIsPostDetailModalOpen(false)}
          post={selectedPost}
          onAddComment={handleAddBulletinComment}
          onDeletePost={handleDeleteBulletinPost}
          currentUser={currentUser}
        />
      )}
    </Card>
  );
};

export const TeamSpacePage: React.FC = () => {
  const { workspaceId, teamProjectId } = useParams<{
    workspaceId: string;
    teamProjectId: string;
  }>();
  const { currentUser, currentTeamProject, setCurrentTeamProject } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [team, setTeam] = useState<TeamProject | null>(null);
  const [loading, setLoading] = useState(true);
  const [password, setPassword] = useState("");
  const [authError, setAuthError] = useState("");
  const [isAuthenticatedForTeam, setIsAuthenticatedForTeam] = useState(false);

  const TABS = [
    { name: "공지", id: "announcements", icon: <ClipboardDocumentListIcon /> },
    { name: "칸반보드", id: "kanban", icon: <TableCellsIcon /> },
    { name: "게시판", id: "bulletin", icon: <Bars3Icon /> },
    { name: "캘린더", id: "calendar", icon: <CalendarDaysIcon /> },
  ] as const;

  type TeamSpaceActiveTabType = (typeof TABS)[number]["id"];

  const getInitialTab = (): TeamSpaceActiveTabType => {
    const queryParams = new URLSearchParams(location.search);
    const feature = queryParams.get("feature");
    if (feature && TABS.some((tab) => tab.id === feature)) {
      return feature as TeamSpaceActiveTabType;
    }
    return "announcements";
  };
  const [activeTab, setActiveTab] = useState<TeamSpaceActiveTabType>(
    getInitialTab()
  );

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const feature = queryParams.get("feature");
    if (
      feature &&
      TABS.some((tab) => tab.id === feature) &&
      feature !== activeTab
    ) {
      setActiveTab(feature as TeamSpaceActiveTabType);
    }
  }, [location.search, activeTab]);

  useEffect(() => {
    if (teamProjectId) {
      if (currentTeamProject && currentTeamProject.id === teamProjectId) {
        setTeam(currentTeamProject);
        if (!currentTeamProject.passwordProtected)
          setIsAuthenticatedForTeam(true);
        setLoading(false);
      } else {
        setLoading(true);
        setTimeout(() => {
          const foundTeam = MOCK_TEAM_PROJECTS_ALL_DETAIL.find(
            (t) => t.id === teamProjectId && t.workspaceId === workspaceId
          );
          if (foundTeam) {
            setTeam(foundTeam);
            setCurrentTeamProject(foundTeam);
            if (!foundTeam.passwordProtected) {
              setIsAuthenticatedForTeam(true);
            } else {
              setIsAuthenticatedForTeam(false);
            }
          } else {
            setTeam(null);
          }
          setLoading(false);
        }, 300);
      }
    } else {
      setTeam(null);
      setLoading(false);
    }
  }, [workspaceId, teamProjectId, currentTeamProject, setCurrentTeamProject]);

  const handlePasswordSubmit = () => {
    if (password === "password123" && team?.passwordProtected) {
      setIsAuthenticatedForTeam(true);
      setAuthError("");
    } else {
      setAuthError("잘못된 비밀번호입니다.");
    }
  };

  if (loading)
    return <div className="p-6 text-center">팀 정보를 불러오는 중...</div>;
  if (!team)
    return (
      <div className="p-6 text-center text-red-500">
        팀을 찾을 수 없습니다.{" "}
        <Link
          to={`/ws/${workspaceId || ""}`}
          className="text-primary hover:underline"
        >
          워크스페이스 홈으로
        </Link>
      </div>
    );
  if (!currentUser) return <p className="p-6">로그인이 필요합니다.</p>;

  if (team.passwordProtected && !isAuthenticatedForTeam) {
    return (
      <Modal
        isOpen={true}
        onClose={() => navigate(`/ws/${workspaceId}`)}
        title={`${team.name} - 비밀번호 입력`}
        footer={<Button onClick={handlePasswordSubmit}>입장</Button>}
      >
        <p className="mb-4 text-sm text-neutral-600">
          이 팀 스페이스는 비밀번호로 보호되어 있습니다.
        </p>
        <Input
          type="password"
          placeholder="팀 비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={authError}
        />
      </Modal>
    );
  }

  let contentToRender;
  switch (activeTab) {
    case "announcements":
      contentToRender = (
        <TeamAnnouncementBoard
          teamProjectId={team.id}
          currentUser={currentUser}
        />
      );
      break;
    case "calendar":
      contentToRender = (
        <TeamCalendar teamProjectId={team.id} currentUser={currentUser} />
      );
      break;
    case "kanban":
      contentToRender = (
        <TeamKanbanBoard teamProjectId={team.id} currentUser={currentUser} />
      );
      break;
    case "bulletin":
      contentToRender = (
        <TeamBulletinBoard teamProjectId={team.id} currentUser={currentUser} />
      );
      break;
    default:
      contentToRender = <p>선택된 기능이 없습니다.</p>;
  }

  return (
    <div className="space-y-6">
      <Card
        title={`팀 스페이스: ${team.name}`}
        actions={
          team.progress !== undefined ? (
            <span className="text-sm text-neutral-500">
              진행도: {team.progress}%
            </span>
          ) : null
        }
      >
        <div className="mb-6 border-b border-neutral-200">
          <nav
            className="-mb-px flex space-x-1 sm:space-x-2 overflow-x-auto"
            aria-label="Tabs"
          >
            {TABS.map((tab) => (
              <button
                key={tab.name}
                onClick={() => setActiveTab(tab.id)}
                className={`whitespace-nowrap py-3 px-2 sm:px-3 border-b-2 font-medium text-xs sm:text-sm flex items-center space-x-1
                ${
                  activeTab === tab.id
                    ? "border-primary text-primary"
                    : "border-transparent text-neutral-500 hover:text-neutral-700 hover:border-neutral-300"
                }`}
              >
                {React.cloneElement(tab.icon, {
                  className: "w-4 h-4 sm:w-5 sm:h-5",
                })}
                <span>{tab.name}</span>
              </button>
            ))}
          </nav>
        </div>
        {contentToRender}
      </Card>
    </div>
  );
};
