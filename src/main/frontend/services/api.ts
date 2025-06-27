import axios from "axios";
import {
  PostResponse,
  CommentResponse,
  ScheduleResponse,
  PagedResponse,
  PostCreateRequest,
  PostUpdateRequest,
  CommentCreateRequest,
  CommentUpdateRequest,
  ScheduleCreateRequest,
  ScheduleUpdateRequest,
} from "./types";

const API_BASE_URL = "http://localhost:8081/api";

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 응답 인터셉터로 에러 처리
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error("API Error:", error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// 게시판 API
export const postApi = {
  // 게시글 목록 조회
  getPosts: async (
    teamId: number,
    boardId: number,
    page = 0,
    size = 20
  ): Promise<PagedResponse<PostResponse>> => {
    try {
      const response = await apiClient.get(
        `/teams/${teamId}/posts?boardId=${boardId}&page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      console.error("게시글 목록 조회 실패:", error);
      throw error;
    }
  },

  // 게시글 상세 조회
  getPost: async (postId: number): Promise<PostResponse> => {
    try {
      const response = await apiClient.get(`/posts/${postId}`);
      return response.data;
    } catch (error) {
      console.error("게시글 상세 조회 실패:", error);
      throw error;
    }
  },

  // 게시글 생성
  createPost: async (
    teamId: number,
    accountId: number,
    postData: PostCreateRequest
  ): Promise<PostResponse> => {
    try {
      const response = await apiClient.post(
        `/teams/${teamId}/posts?accountId=${accountId}`,
        postData
      );
      return response.data;
    } catch (error) {
      console.error("게시글 생성 실패:", error);
      throw error;
    }
  },

  // 게시글 수정
  updatePost: async (
    postId: number,
    accountId: number,
    postData: PostUpdateRequest
  ): Promise<PostResponse> => {
    try {
      const response = await apiClient.patch(
        `/posts/${postId}?accountId=${accountId}`,
        postData
      );
      return response.data;
    } catch (error) {
      console.error("게시글 수정 실패:", error);
      throw error;
    }
  },

  // 게시글 삭제
  deletePost: async (postId: number, accountId: number): Promise<void> => {
    try {
      await apiClient.delete(`/posts/${postId}?accountId=${accountId}`);
    } catch (error) {
      console.error("게시글 삭제 실패:", error);
      throw error;
    }
  },
};

// 댓글 API
export const commentApi = {
  // 댓글 목록 조회
  getComments: async (
    postId: number,
    page = 0,
    size = 20
  ): Promise<PagedResponse<CommentResponse>> => {
    try {
      const response = await apiClient.get(
        `/posts/${postId}/comments?page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      console.error("댓글 목록 조회 실패:", error);
      throw error;
    }
  },

  // 댓글 생성
  createComment: async (
    postId: number,
    accountId: number,
    commentData: CommentCreateRequest
  ): Promise<CommentResponse> => {
    try {
      const response = await apiClient.post(
        `/posts/${postId}/comments?accountId=${accountId}`,
        commentData
      );
      return response.data;
    } catch (error) {
      console.error("댓글 생성 실패:", error);
      throw error;
    }
  },

  // 댓글 수정
  updateComment: async (
    commentId: number,
    accountId: number,
    commentData: CommentUpdateRequest
  ): Promise<CommentResponse> => {
    try {
      const response = await apiClient.patch(
        `/comments/${commentId}?accountId=${accountId}`,
        commentData
      );
      return response.data;
    } catch (error) {
      console.error("댓글 수정 실패:", error);
      throw error;
    }
  },

  // 댓글 삭제
  deleteComment: async (
    commentId: number,
    accountId: number
  ): Promise<void> => {
    try {
      await apiClient.delete(`/comments/${commentId}?accountId=${accountId}`);
    } catch (error) {
      console.error("댓글 삭제 실패:", error);
      throw error;
    }
  },
};

// 스케줄 API
export const scheduleApi = {
  // 스케줄 목록 조회
  getSchedules: async (
    teamId: number,
    page = 0,
    size = 20,
    type?: string
  ): Promise<PagedResponse<ScheduleResponse>> => {
    try {
      let url = `/teams/${teamId}/schedules?page=${page}&size=${size}`;
      if (type) {
        url += `&type=${type}`;
      }

      const response = await apiClient.get(url);
      return response.data;
    } catch (error) {
      console.error("스케줄 목록 조회 실패:", error);
      throw error;
    }
  },

  // 기간별 스케줄 조회
  getSchedulesByDateRange: async (
    teamId: number,
    startDate: string,
    endDate: string
  ): Promise<ScheduleResponse[]> => {
    try {
      // LocalDateTime format (yyyy-MM-ddTHH:mm:ss) - 성공 확인된 포맷
      const startDateObj = new Date(startDate);
      const endDateObj = new Date(endDate);
      const formattedStartDate = startDateObj.toISOString().slice(0, 19);
      const formattedEndDate = endDateObj.toISOString().slice(0, 19);

      console.log("📅 Calendar API Call:", {
        teamId,
        startDate: formattedStartDate,
        endDate: formattedEndDate,
      });

      const response = await apiClient.get(`/teams/${teamId}/schedules/range`, {
        params: {
          startDate: formattedStartDate,
          endDate: formattedEndDate,
        },
      });

      console.log("✅ Calendar API Success:", response.data);
      return response.data;
    } catch (error: any) {
      console.error("❌ Calendar API Error:", {
        status: error.response?.status,
        message: error.response?.data?.message || error.message,
        teamId,
      });
      return [];
    }
  },

  // 내 스케줄 조회
  getMySchedules: async (
    accountId: number,
    page = 0,
    size = 20
  ): Promise<PagedResponse<ScheduleResponse>> => {
    try {
      const response = await apiClient.get(
        `/teams/1/schedules/my?accountId=${accountId}&page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      console.error("내 스케줄 조회 실패:", error);
      throw error;
    }
  },

  // 스케줄 생성
  createSchedule: async (
    teamId: number,
    accountId: number,
    scheduleData: ScheduleCreateRequest
  ): Promise<ScheduleResponse> => {
    try {
      const response = await apiClient.post(
        `/teams/${teamId}/schedules?accountId=${accountId}`,
        scheduleData
      );
      return response.data;
    } catch (error) {
      console.error("스케줄 생성 실패:", error);
      throw error;
    }
  },

  // 스케줄 수정
  updateSchedule: async (
    teamId: number,
    scheduleId: number,
    accountId: number,
    scheduleData: ScheduleUpdateRequest
  ): Promise<ScheduleResponse> => {
    try {
      const response = await apiClient.put(
        `/teams/${teamId}/schedules/${scheduleId}?accountId=${accountId}`,
        scheduleData
      );
      return response.data;
    } catch (error) {
      console.error("스케줄 수정 실패:", error);
      throw error;
    }
  },

  // 스케줄 삭제
  deleteSchedule: async (
    teamId: number,
    scheduleId: number,
    accountId: number
  ): Promise<void> => {
    try {
      await apiClient.delete(
        `/teams/${teamId}/schedules/${scheduleId}?accountId=${accountId}`
      );
    } catch (error) {
      console.error("스케줄 삭제 실패:", error);
      throw error;
    }
  },
};

// 파일 업로드 API
export const fileApi = {
  // 게시글의 첨부파일 업로드 (백엔드 API 구조에 맞게 수정)
  uploadAttachment: async (
    postId: number,
    accountId: number,
    file: File
  ): Promise<any> => {
    try {
      const formData = new FormData();
      formData.append("file", file);

      const response = await apiClient.post(
        `/posts/${postId}/attachments?accountId=${accountId}`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );
      return response.data;
    } catch (error) {
      console.error("첨부파일 업로드 실패:", error);
      throw error;
    }
  },

  // 파일 다운로드 URL 생성 (백엔드 FileController에 맞게 수정)
  getDownloadUrl: (fileId: string): string => {
    return `${API_BASE_URL}/files/${fileId}/download`;
  },

  // 게시글에 첨부파일과 함께 생성 (새로운 방식)
  createPostWithAttachments: async (
    teamId: number,
    accountId: number,
    postData: PostCreateRequest,
    files: File[]
  ): Promise<PostResponse> => {
    try {
      // 1. 먼저 게시글을 생성 (첨부파일 없이)
      const response = await apiClient.post(
        `/teams/${teamId}/posts?accountId=${accountId}`,
        postData
      );
      const createdPost = response.data;

      // 2. 생성된 게시글에 파일들을 업로드
      if (files.length > 0) {
        await Promise.all(
          files.map((file) =>
            fileApi.uploadAttachment(createdPost.id, accountId, file)
          )
        );
      }

      return createdPost;
    } catch (error) {
      console.error("첨부파일과 함께 게시글 생성 실패:", error);
      throw error;
    }
  },
};

// 모든 API를 하나의 객체로 내보내기
export const api = {
  posts: postApi,
  comments: commentApi,
  schedules: scheduleApi,
  files: fileApi,
};

// 기본 내보내기
export default api;
