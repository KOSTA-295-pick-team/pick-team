
import React, { ReactNode, useState, useEffect, useMemo } from 'react';
import { HashRouter, Routes, Route, Link, Navigate, Outlet, useLocation, useParams, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './AuthContext';
import { LoginPage, SignupPage } from './pages/AuthPage';
import { MyPage, ProfileEditPage, AccountSettingsPage } from './pages/MyPageStack';
import { UserProfileViewPage } from './pages/UserProfilePage';
import { AutoTeamPage, VoteTeamPage, AdminTeamPage, TeamFormationHubPage } from './pages/TeamFormationStack';
import { TeamSpacePage } from './pages/TeamSpacePage';
import { NotificationsPage } from './pages/NotificationsPage';
import { HomePage } from './pages/HomePage';
import { ChatPage } from './pages/ChatPage';
import { VideoConferencePage } from './pages/VideoConferencePage';
import { 
    UserIcon, UsersIcon, BellIcon, LogoutIcon, CogIcon, PlusCircleIcon,
    ChatBubbleIcon, VideoCameraIcon, CalendarDaysIcon, Modal, Input, Button, ItemListSelector,
    TrashIcon as ComponentTrashIcon, // Renamed to avoid conflict
    TextArea
} from './components';
import { Workspace, TeamProject, User as UserType, ChatRoom, ChatRoomMember } from './types';
import { MagnifyingGlassIcon, HashtagIcon, LockClosedIcon, UserGroupIcon, ArrowLeftIcon, Cog6ToothIcon, LinkIcon, ShieldCheckIcon, UserMinusIcon, NoSymbolIcon, CheckCircleIcon, XMarkIcon, ChevronDownIcon } from '@heroicons/react/24/outline';


// Mock Data for non-chat related entities
const MOCK_WORKSPACES_APP: Workspace[] = [
  { id: 'ws_kosta', name: 'kosta 2957', ownerId: 'user1', iconUrl: 'K', 
    members: [
        {id: 'user@example.com', name: '테스트 사용자', profilePictureUrl: 'https://picsum.photos/seed/user1/40/40'}, 
        {id: 'user_kim', name: '김코딩', profilePictureUrl: 'https://picsum.photos/seed/userA/40/40'}, 
        {id: 'user_park', name: '박해커', profilePictureUrl: 'https://picsum.photos/seed/userB/40/40'}
    ],
    inviteCode: 'kosta2957-invite-xyz'
  },
  { id: 'ws_da', name: '다목적 공간', ownerId: 'user1', iconUrl: '다', 
    members: [
        {id: 'user@example.com', name: '테스트 사용자', profilePictureUrl: 'https://picsum.photos/seed/user1/40/40'},
        {id: 'user_lee', name: '이디자인', profilePictureUrl: 'https://picsum.photos/seed/userC/40/40'}
    ],
    inviteCode: 'da-invite-abc'
   },
  { id: 'ws_gaein', name: '개인용', ownerId: 'user1', iconUrl: '개', 
    members: [ {id: 'user@example.com', name: '테스트 사용자', profilePictureUrl: 'https://picsum.photos/seed/user1/40/40'}],
    inviteCode: 'gaein-invite-123'
  },
];

const MOCK_TEAM_PROJECTS_APP: TeamProject[] = [
  { id: 'tp_alpha', workspaceId: 'ws_kosta', name: '알파 프로젝트 팀', members: [], announcements: [], memberCount: 2, progress: 75 },
  { id: 'tp_beta', workspaceId: 'ws_kosta', name: '베타 서비스 개발팀', members: [], announcements: [], memberCount: 1, progress: 40 },
  { id: 'tp_personal', workspaceId: 'ws_kosta', name: '개인프로젝트 팀', members: [], announcements: [], memberCount: 2, progress: 90 },
  { id: 'tp_gongji_features', workspaceId: 'ws_kosta', name: '#공지 전용 (기능)', members: [], announcements: [], memberCount: 0}, 

  { id: 'tp_project_x', workspaceId: 'ws_da', name: 'Project X', members: [], announcements: [], memberCount: 3, progress: 50 },
  { id: 'tp_idea_lab', workspaceId: 'ws_da', name: '아이디어 실험실', members: [], announcements: [], memberCount: 1, progress: 20 },
];


const ProtectedRoute: React.FC<{ children?: ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children ? <>{children}</> : <AppLayout />; 
};

const GlobalHeader: React.FC = () => {
  const { currentUser, logout, currentWorkspace } = useAuth();
  const [profileDropdownOpen, setProfileDropdownOpen] = useState(false);
  const navigate = useNavigate();

  if (!currentUser) return null;

  return (
    <header className="bg-primary text-white shadow-md h-16 flex items-center justify-between px-4 sm:px-6 lg:px-8 fixed top-0 left-0 right-0 z-50">
      <Link to={`/ws/${currentWorkspace?.id || MOCK_WORKSPACES_APP[0].id}`} className="text-xl font-bold">
        팀플메이트
      </Link>
      <div className="flex items-center space-x-4">
        {/* 팀 구성 버튼 삭제됨 */}
        <Link to="/notifications" className="p-1 rounded-full hover:bg-primary-dark relative">
          <BellIcon className="h-6 w-6" />
        </Link>
        <div className="relative">
          <button 
            onClick={() => setProfileDropdownOpen(!profileDropdownOpen)}
            className="rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-primary-dark focus:ring-white"
          >
            <img className="h-8 w-8 rounded-full object-cover" src={currentUser.profilePictureUrl || `https://picsum.photos/seed/${currentUser.id}/32/32`} alt="User Profile" />
          </button>
          {profileDropdownOpen && (
            <div className="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-white ring-1 ring-black ring-opacity-5 focus:outline-none text-neutral-700">
              <Link to="/my-page" className="block px-4 py-2 text-sm hover:bg-neutral-100" onClick={() => setProfileDropdownOpen(false)}>마이페이지</Link>
              <button 
                onClick={() => { 
                  logout(); 
                  setProfileDropdownOpen(false);
                  navigate('/login'); 
                }} 
                className="w-full text-left block px-4 py-2 text-sm hover:bg-neutral-100"
              >
                로그아웃
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

const JoinWorkspaceModal: React.FC<{ isOpen: boolean; onClose: () => void }> = ({ isOpen, onClose }) => {
    const [inviteUrl, setInviteUrl] = useState('');
    const [error, setError] = useState('');
    const { setCurrentWorkspace } = useAuth();
    const navigate = useNavigate();

    const handleCloseAndReset = () => {
        setInviteUrl('');
        setError('');
        onClose();
    };

    const handleJoinWorkspace = () => {
        setError('');
        try {
            const url = new URL(inviteUrl);
            const params = new URLSearchParams(url.search);
            const wsIdFromUrl = params.get('ws');

            if (!wsIdFromUrl) {
                setError("초대 링크에서 워크스페이스 ID를 찾을 수 없습니다.");
                return;
            }

            const foundWorkspace = MOCK_WORKSPACES_APP.find(ws => ws.id === wsIdFromUrl);

            if (foundWorkspace) {
                setCurrentWorkspace(foundWorkspace);
                navigate(`/ws/${foundWorkspace.id}`);
                handleCloseAndReset();
            } else {
                setError("해당 ID의 워크스페이스를 찾을 수 없거나 링크가 유효하지 않습니다.");
            }
        } catch (e) {
            setError("유효하지 않은 URL 형식입니다. 전체 초대 URL을 입력해주세요 (예: https://teammate.app/join?ws=...).");
            console.error("Invalid URL:", e);
        }
    };
    
    return (
        <Modal 
            isOpen={isOpen} 
            onClose={handleCloseAndReset} 
            title="워크스페이스 참여하기"
            footer={
                <div className="flex justify-end space-x-2">
                    <Button variant="ghost" onClick={handleCloseAndReset}>취소</Button>
                    <Button onClick={handleJoinWorkspace}>참여하기</Button>
                </div>
            }
        >
            <div className="space-y-4">
                <p className="text-sm text-neutral-600">워크스페이스 초대 URL을 입력해주세요.</p>
                <Input
                    type="url"
                    placeholder="예: https://teammate.app/join?ws=워크스페이스ID&invite_code=..."
                    value={inviteUrl}
                    onChange={(e) => setInviteUrl(e.target.value)}
                    error={error}
                    Icon={LinkIcon}
                />
            </div>
        </Modal>
    );
};


const WorkspaceSidebar: React.FC = () => {
  const { currentWorkspace, setCurrentWorkspace, currentUser } = useAuth();
  const navigate = useNavigate();
  const [isJoinWorkspaceModalOpen, setIsJoinWorkspaceModalOpen] = useState(false);

  const selectWorkspace = (ws: Workspace) => {
    if (currentUser) {
      setCurrentWorkspace(ws);
      navigate(`/ws/${ws.id}`);
    }
  };
  
  return (
    <>
    <nav className="bg-neutral-800 text-neutral-300 w-16 flex flex-col items-center py-4 space-y-3 fixed top-16 left-0 h-[calc(100vh-4rem)] z-40">
      {MOCK_WORKSPACES_APP.map(ws => (
        <button
          key={ws.id}
          onClick={() => selectWorkspace(ws)}
          title={ws.name}
          className={`w-10 h-10 rounded-lg flex items-center justify-center text-xl font-bold transition-all
            ${currentWorkspace?.id === ws.id ? 'bg-primary text-white scale-110 ring-2 ring-white' : 'bg-neutral-700 hover:bg-neutral-600 focus:bg-neutral-600'}
            focus:outline-none`}
        >
          {ws.iconUrl || ws.name.charAt(0).toUpperCase()}
        </button>
      ))}
      <button 
        title="워크스페이스 참여"
        className="w-10 h-10 rounded-lg flex items-center justify-center bg-neutral-700 hover:bg-neutral-600 focus:bg-neutral-600 mt-auto focus:outline-none"
        onClick={() => setIsJoinWorkspaceModalOpen(true)}
      >
        <PlusCircleIcon className="w-6 h-6"/>
      </button>
    </nav>
    <JoinWorkspaceModal isOpen={isJoinWorkspaceModalOpen} onClose={() => setIsJoinWorkspaceModalOpen(false)} />
    </>
  );
};

const NewChatModal: React.FC<{isOpen: boolean, onClose: () => void}> = ({isOpen, onClose}) => {
    const { currentUser, currentWorkspace, createChatRoom, allUsersForChat, setCurrentChatRoomById } = useAuth();
    const navigate = useNavigate();
    const [chatType, setChatType] = useState<'dm' | 'group'>('dm');
    const [groupName, setGroupName] = useState('');
    const [selectedUsers, setSelectedUsers] = useState<UserType[]>([]);

    const availableUsersForSelection = useMemo(() => {
        return allUsersForChat.filter(u => u.id !== currentUser?.id);
    }, [allUsersForChat, currentUser]);

    const handleUserSelect = (user: UserType) => {
        if (chatType === 'dm') {
            setSelectedUsers([user]); 
        } else {
            setSelectedUsers(prev => 
                prev.find(su => su.id === user.id) ? prev.filter(su => su.id !== user.id) : [...prev, user]
            );
        }
    };
    
    const renderUserItemForSelection = (user: UserType, isSelected: boolean) => (
        <div className="flex items-center space-x-2">
            <img src={user.profilePictureUrl || `https://picsum.photos/seed/${user.id}/30/30`} alt={user.name} className="w-6 h-6 rounded-full" />
            <span>{user.name}</span>
            {isSelected && <span className="text-primary ml-auto">✓</span>}
        </div>
    );

    const handleCreate = async () => {
        if (!currentUser || !currentWorkspace) {
            alert("사용자 또는 워크스페이스 정보를 찾을 수 없습니다.");
            return;
        }
        
        const currentUserAsMember: ChatRoomMember = {
            id: currentUser.id,
            name: currentUser.name,
            profilePictureUrl: currentUser.profilePictureUrl
        };

        const selectedUsersAsMembers: ChatRoomMember[] = selectedUsers.map(u => ({
            id: u.id,
            name: u.name,
            profilePictureUrl: u.profilePictureUrl
        }));
        
        let membersToCreateWith: ChatRoomMember[] = [];
        if (chatType === 'dm') {
            if (selectedUsersAsMembers.length !== 1) {
                alert("DM을 시작할 사용자 1명을 선택해주세요.");
                return;
            }
            membersToCreateWith = [currentUserAsMember, selectedUsersAsMembers[0]];
        } else { // Group chat
            if (groupName.trim() === '') {
                alert("그룹 채팅방 이름을 입력해주세요.");
                return;
            }
            if (selectedUsersAsMembers.length === 0) { // Check if any *additional* members are selected
                alert("그룹 멤버를 1명 이상 선택해주세요."); 
                return;
            }
             membersToCreateWith = [currentUserAsMember, ...selectedUsersAsMembers];
        }

        const newRoom = await createChatRoom(groupName, membersToCreateWith, chatType);
        if (newRoom) {
            setCurrentChatRoomById(newRoom.id);
            navigate(`/ws/${currentWorkspace.id}/chat/${newRoom.id}`);
            onCloseModal();
        } else {
            alert("채팅방 생성에 실패했습니다. 이미 DM이 존재하거나 그룹 이름/멤버 조건이 맞지 않을 수 있습니다.");
        }
    };
    
    const onCloseModal = () => {
        setGroupName('');
        setSelectedUsers([]);
        setChatType('dm');
        onClose();
    }
    
    useEffect(() => { 
      setSelectedUsers([]);
    }, [chatType]);

    return (
        <Modal isOpen={isOpen} onClose={onCloseModal} title="새로운 채팅 시작하기" footer={
            <div className="flex justify-end space-x-2">
                <Button variant="ghost" onClick={onCloseModal}>취소</Button>
                <Button onClick={handleCreate}>채팅 시작</Button>
            </div>
        }>
            <div className="space-y-4">
                <div className="flex space-x-2">
                    <Button variant={chatType === 'dm' ? 'primary' : 'outline'} onClick={() => setChatType('dm')} className="flex-1">1:1 대화</Button>
                    <Button variant={chatType === 'group' ? 'primary' : 'outline'} onClick={() => setChatType('group')} className="flex-1">그룹 대화</Button>
                </div>

                {chatType === 'group' && (
                    <Input label="그룹 채팅방 이름" value={groupName} onChange={e => setGroupName(e.target.value)} placeholder="예: 프로젝트 논의방"/>
                )}

                <div>
                    <label className="block text-sm font-medium text-neutral-700 mb-1">
                        {chatType === 'dm' ? '대화 상대 선택 (1명)' : '그룹 멤버 선택'}
                    </label>
                    <ItemListSelector
                        items={availableUsersForSelection}
                        selectedItems={selectedUsers}
                        onSelectItem={handleUserSelect}
                        renderItem={renderUserItemForSelection}
                        itemKey="id"
                    />
                </div>
            </div>
        </Modal>
    );
};

const NewVideoConferenceModal: React.FC<{isOpen: boolean, onClose: () => void}> = ({isOpen, onClose}) => {
    const { currentWorkspace } = useAuth();
    const navigate = useNavigate();
    const [roomName, setRoomName] = useState('');

    const handleStartConference = () => {
        if (!roomName.trim()) {
            alert("회의실 이름을 입력해주세요.");
            return;
        }
        if (!currentWorkspace) {
            alert("워크스페이스 정보를 찾을 수 없습니다.");
            return;
        }
        navigate(`/ws/${currentWorkspace.id}/video/live?room=${encodeURIComponent(roomName.trim())}`);
        setRoomName('');
        onClose();
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="새 화상회의 시작" footer={
            <div className="flex justify-end space-x-2">
                <Button variant="ghost" onClick={onClose}>취소</Button>
                <Button onClick={handleStartConference}>화상회의 시작</Button>
            </div>
        }>
            <Input 
                label="화상회의 방 이름" 
                value={roomName} 
                onChange={e => setRoomName(e.target.value)} 
                placeholder="예: 주간 팀 회의"
                required
            />
        </Modal>
    );
};

const TeamActionModal: React.FC<{isOpen: boolean, onClose: () => void}> = ({isOpen, onClose}) => {
    const { currentWorkspace, setCurrentTeamProject } = useAuth();
    const navigate = useNavigate();
    const [step, setStep] = useState<'initial' | 'joinList'>('initial');
    const [selectedTeamToJoin, setSelectedTeamToJoin] = useState<TeamProject | null>(null);

    const availableTeamsToJoin = useMemo(() => {
        if (!currentWorkspace) return [];
        return MOCK_TEAM_PROJECTS_APP.filter(
            tp => tp.workspaceId === currentWorkspace.id && !tp.name.startsWith('#') // Exclude special channels
        );
    }, [currentWorkspace]);

    const handleCloseAndReset = () => {
        onClose();
        setStep('initial');
        setSelectedTeamToJoin(null);
    };

    const handleJoinTeam = () => {
        if (selectedTeamToJoin && currentWorkspace) {
            alert(`${selectedTeamToJoin.name} 팀에 참여했습니다! (목업)`);
            setCurrentTeamProject(selectedTeamToJoin); // Set context for joined team
            navigate(`/ws/${currentWorkspace.id}/team/${selectedTeamToJoin.id}`);
            handleCloseAndReset();
        } else {
            alert("팀을 선택해주세요 또는 워크스페이스 정보가 없습니다.");
        }
    };

    const renderTeamItem = (team: TeamProject, isSelected: boolean) => (
      <div className="flex items-center justify-between">
        <span>{team.name} <span className="text-xs text-neutral-500">({team.memberCount || 0}명)</span></span>
        {isSelected && <span className="text-primary">✓</span>}
      </div>
    );
    
    let modalTitle = "팀 생성 또는 참여";
    let modalFooter;
    let modalContent;

    if (step === 'initial') {
        modalFooter = (
            <div className="flex justify-end space-x-2">
                <Button variant="ghost" onClick={handleCloseAndReset}>취소</Button>
            </div>
        );
        modalContent = (
            <div className="space-y-3">
                <Button 
                    className="w-full" 
                    onClick={() => { navigate('/team-formation'); handleCloseAndReset(); }}
                >
                    새 팀 만들기
                </Button>
                <Button 
                    className="w-full" 
                    variant="outline" 
                    onClick={() => setStep('joinList')}
                    disabled={!currentWorkspace || availableTeamsToJoin.length === 0}
                >
                    기존 팀 참여하기
                </Button>
                {(!currentWorkspace || availableTeamsToJoin.length === 0) && (
                    <p className="text-xs text-neutral-500 text-center">현재 워크스페이스에 참여할 수 있는 팀이 없습니다.</p>
                )}
            </div>
        );
    } else { // step === 'joinList'
        modalTitle = "기존 팀에 참여하기";
        modalFooter = (
            <div className="flex justify-between w-full">
                <Button variant="ghost" onClick={() => { setStep('initial'); setSelectedTeamToJoin(null); }} leftIcon={<ArrowLeftIcon className="w-4 h-4"/>}>
                    뒤로
                </Button>
                <div className="space-x-2">
                    <Button variant="ghost" onClick={handleCloseAndReset}>취소</Button>
                    <Button onClick={handleJoinTeam} disabled={!selectedTeamToJoin}>참여하기</Button>
                </div>
            </div>
        );
        modalContent = (
            <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">참여할 팀 선택:</label>
                <ItemListSelector
                    items={availableTeamsToJoin}
                    selectedItems={selectedTeamToJoin ? [selectedTeamToJoin] : []}
                    onSelectItem={(item: TeamProject) => setSelectedTeamToJoin(item)}
                    renderItem={renderTeamItem}
                    itemKey="id"
                />
            </div>
        );
    }

    return (
        <Modal isOpen={isOpen} onClose={handleCloseAndReset} title={modalTitle} footer={modalFooter}>
            {modalContent}
        </Modal>
    );
};

const WorkspaceSettingsModal: React.FC<{isOpen: boolean, onClose: () => void}> = ({isOpen, onClose}) => {
    const { currentWorkspace, currentUser } = useAuth();
    const [activeTab, setActiveTab] = useState<'invite' | 'members' | 'security'>('invite');
    const [workspacePassword, setWorkspacePassword] = useState('');
    const [showConfirmDelete, setShowConfirmDelete] = useState<{type: string, id: string, name: string} | null>(null);

    useEffect(() => {
        if (currentWorkspace) {
            setWorkspacePassword(currentWorkspace.password || '');
        }
    }, [currentWorkspace]);

    if (!currentWorkspace || !currentUser) return null;

    const handleCopyLink = () => {
        const inviteLink = `https://teammate.app/join?ws=${currentWorkspace.id}&invite_code=${currentWorkspace.inviteCode || 'mock-code-123'}`;
        navigator.clipboard.writeText(inviteLink)
            .then(() => alert('초대 링크가 복사되었습니다!'))
            .catch(() => alert('링크 복사에 실패했습니다.'));
    };
    
    const handleSavePassword = () => {
        alert(`워크스페이스 비밀번호가 '${workspacePassword}' (으)로 설정(업데이트) 되었습니다. (목업)`);
        // In real app: currentWorkspace.password = workspacePassword; // Then update context/backend
        onClose();
    };

    const handleKickMember = (memberId: string, memberName: string) => {
        setShowConfirmDelete({type: 'kick', id: memberId, name: memberName});
    };
    
    const handleBanMember = (memberId: string, memberName: string) => {
         setShowConfirmDelete({type: 'ban', id: memberId, name: memberName});
    };

    const confirmAction = () => {
        if(showConfirmDelete){
            alert(`${showConfirmDelete.name}님을 워크스페이스에서 ${showConfirmDelete.type === 'kick' ? '추방' : '차단'}했습니다. (목업)`);
            // Real app: update member list in currentWorkspace
        }
        setShowConfirmDelete(null);
    }


    const TABS_CONFIG = [
        { id: 'invite', label: '초대', icon: <LinkIcon className="w-5 h-5 mr-2" /> },
        { id: 'members', label: '멤버 관리', icon: <UsersIcon className="w-5 h-5 mr-2" /> },
        { id: 'security', label: '보안', icon: <ShieldCheckIcon className="w-5 h-5 mr-2" /> },
    ];


    return (
        <>
        <Modal isOpen={isOpen} onClose={onClose} title={`${currentWorkspace.name} 설정`}
            footer={ activeTab === 'security' ? (
                <div className="flex justify-end space-x-2">
                    <Button variant="ghost" onClick={onClose}>취소</Button>
                    <Button onClick={handleSavePassword}>비밀번호 저장</Button>
                </div>
            ) : <Button variant="primary" onClick={onClose}>닫기</Button>
        }>
            <div className="mb-4 border-b border-neutral-200">
                <nav className="-mb-px flex space-x-2" aria-label="Tabs">
                    {TABS_CONFIG.map((tab) => (
                    <button
                        key={tab.id}
                        onClick={() => setActiveTab(tab.id as any)}
                        className={`whitespace-nowrap py-3 px-3 border-b-2 font-medium text-sm flex items-center
                        ${activeTab === tab.id
                            ? 'border-primary text-primary'
                            : 'border-transparent text-neutral-500 hover:text-neutral-700 hover:border-neutral-300'}`}
                    >
                        {tab.icon}
                        {tab.label}
                    </button>
                    ))}
                </nav>
            </div>

            {activeTab === 'invite' && (
                <div className="space-y-4">
                    <p className="text-sm text-neutral-600">워크스페이스에 팀원을 초대하세요. 아래 링크를 공유해주세요.</p>
                    <Input 
                        value={`https://teammate.app/join?ws=${currentWorkspace.id}&invite_code=${currentWorkspace.inviteCode || 'mock-code-123'}`} 
                        readOnly 
                        Icon={LinkIcon}
                    />
                    <div className="flex space-x-2">
                        <Button onClick={handleCopyLink} className="flex-1">링크 복사</Button>
                        <Button variant="outline" onClick={() => alert('새 초대 링크가 생성되었습니다. (목업)')} className="flex-1">새 링크 생성</Button>
                    </div>
                </div>
            )}

            {activeTab === 'members' && (
                <div className="space-y-3">
                    <p className="text-sm text-neutral-600 mb-2">{currentWorkspace.members.length}명의 멤버</p>
                    <div className="max-h-60 overflow-y-auto pr-1 space-y-2">
                    {currentWorkspace.members.map(member => (
                        <div key={member.id} className="flex items-center justify-between p-2 bg-neutral-50 rounded-md">
                            <div className="flex items-center space-x-2">
                                <img src={member.profilePictureUrl || `https://picsum.photos/seed/${member.id}/32/32`} alt={member.name} className="w-8 h-8 rounded-full"/>
                                <span>{member.name} {member.id === currentUser.id && <span className="text-xs text-primary">(나)</span>}</span>
                            </div>
                            {member.id !== currentUser.id && ( // Cannot kick/ban self
                                <div className="space-x-1">
                                    <Button size="sm" variant="ghost" className="text-orange-600 hover:bg-orange-100" onClick={() => handleKickMember(member.id, member.name || '해당 멤버')} title="추방">
                                        <UserMinusIcon className="w-4 h-4"/>
                                    </Button>
                                    <Button size="sm" variant="ghost" className="text-red-600 hover:bg-red-100" onClick={() => handleBanMember(member.id, member.name || '해당 멤버')} title="차단">
                                        <NoSymbolIcon className="w-4 h-4"/>
                                    </Button>
                                </div>
                            )}
                        </div>
                    ))}
                    </div>
                </div>
            )}

            {activeTab === 'security' && (
                <div className="space-y-4">
                    <Input 
                        label="워크스페이스 비밀번호 설정"
                        type="password"
                        value={workspacePassword}
                        onChange={e => setWorkspacePassword(e.target.value)}
                        placeholder="비밀번호 미설정 시 공개"
                    />
                    <p className="text-xs text-neutral-500">
                        비밀번호를 설정하면, 워크스페이스에 참여하려는 사용자는 이 비밀번호를 입력해야 합니다.
                    </p>
                </div>
            )}
        </Modal>

        {showConfirmDelete && (
            <Modal
                isOpen={!!showConfirmDelete}
                onClose={() => setShowConfirmDelete(null)}
                title={`${showConfirmDelete.type === 'kick' ? '멤버 추방' : '멤버 차단'} 확인`}
                footer={
                    <div className="flex justify-end space-x-2">
                        <Button variant="ghost" onClick={() => setShowConfirmDelete(null)}>취소</Button>
                        <Button variant={showConfirmDelete.type === 'kick' ? 'primary' : 'danger'} onClick={confirmAction}>
                            {showConfirmDelete.type === 'kick' ? '추방' : '차단'}
                        </Button>
                    </div>
                }
            >
                <p>정말로 <strong className="font-semibold">{showConfirmDelete.name}</strong>님을 이 워크스페이스에서 
                {showConfirmDelete.type === 'kick' ? ' 추방하시겠습니까?' : ' 차단하시겠습니까?'}
                </p>
                {showConfirmDelete.type === 'ban' && <p className="text-sm text-red-500 mt-1">차단된 사용자는 이 워크스페이스에 다시 참여할 수 없습니다.</p>}
            </Modal>
        )}
        </>
    );
};


const TeamProjectSidebar: React.FC = () => {
  const { 
    currentWorkspace, currentTeamProject, setCurrentTeamProject, 
    currentUser, chatRooms, currentChatRoom, setCurrentChatRoomById, getChatRoomName, deleteChatRoom
  } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isNewChatModalOpen, setIsNewChatModalOpen] = useState(false);
  const [isNewVideoConferenceModalOpen, setIsNewVideoConferenceModalOpen] = useState(false);
  const [isTeamActionModalOpen, setIsTeamActionModalOpen] = useState(false);
  const [isWorkspaceSettingsModalOpen, setIsWorkspaceSettingsModalOpen] = useState(false);
  
  const [confirmDeleteInfo, setConfirmDeleteInfo] = useState<{type: 'chat' | 'video', id: string, name: string} | null>(null);

  const initialMockVideoRooms = [
    { id: 'vid_daily_scrum', name: '데일리 스크럼'},
    { id: 'vid_design_review', name: '디자인 리뷰'},
    { id: 'vid_client_meeting', name: '클라이언트 미팅'},
  ];
  const [mockVideoRooms, setMockVideoRooms] = useState(initialMockVideoRooms);


  if (!currentWorkspace || !currentUser) return null;

  const teamsForCurrentWorkspace = MOCK_TEAM_PROJECTS_APP.filter(
    tp => tp.workspaceId === currentWorkspace.id && tp.id !== 'tp_gongji_features'
  );
  const groupChats = chatRooms.filter(cr => cr.type === 'group');
  const directMessages = chatRooms.filter(cr => cr.type === 'dm');

  const selectTeamProject = (tp: TeamProject, feature?: string) => {
    setCurrentTeamProject(tp);
    setCurrentChatRoomById(null); 
    let path = `/ws/${currentWorkspace.id}/team/${tp.id}`;
    if (feature) {
        path += `?feature=${feature}`;
    }
    navigate(path);
  };
  
  const selectChatRoom = (roomId: string) => {
    setCurrentChatRoomById(roomId);
    setCurrentTeamProject(null); 
    navigate(`/ws/${currentWorkspace.id}/chat/${roomId}`);
  };

  const selectVideoRoom = (roomName: string) => {
    if (currentWorkspace) {
        navigate(`/ws/${currentWorkspace.id}/video/live?room=${encodeURIComponent(roomName)}`);
    }
  };

  const handleDeleteChatRoom = (roomId: string, roomName: string) => {
    setConfirmDeleteInfo({ type: 'chat', id: roomId, name: roomName });
  };
  
  const handleDeleteVideoRoom = (roomId: string, roomName: string) => {
    setConfirmDeleteInfo({ type: 'video', id: roomId, name: roomName });
  };

  const confirmDeletion = () => {
    if (confirmDeleteInfo) {
      if (confirmDeleteInfo.type === 'chat') {
        deleteChatRoom(confirmDeleteInfo.id);
        alert(`'${confirmDeleteInfo.name}' 채팅방이 삭제(퇴장)되었습니다. (목업)`);
      } else if (confirmDeleteInfo.type === 'video') {
        setMockVideoRooms(prev => prev.filter(room => room.id !== confirmDeleteInfo.id));
        alert(`'${confirmDeleteInfo.name}' 화상회의 채널이 삭제되었습니다. (목업)`);
      }
    }
    setConfirmDeleteInfo(null);
  };


  return (
    <aside className="bg-neutral-100 border-r border-neutral-300 w-64 p-4 space-y-1 fixed top-16 left-16 h-[calc(100vh-4rem)] z-30 overflow-y-auto">
      <div className="flex justify-between items-center mb-1">
        <h2 className="text-lg font-semibold text-neutral-800 truncate" title={currentWorkspace.name}>{currentWorkspace.name}</h2>
        <button 
          onClick={() => setIsWorkspaceSettingsModalOpen(true)}
          className="p-1 text-neutral-500 hover:text-neutral-700 hover:bg-neutral-200 rounded"
          title="워크스페이스 설정"
          aria-label="워크스페이스 설정"
        >
          <Cog6ToothIcon className="w-5 h-5"/>
        </button>
      </div>
      
      <div className="mb-3">
        <h3 className="text-xs font-semibold text-neutral-500 uppercase tracking-wider mb-1 px-2">팀 프로젝트 & 기능</h3>
        {teamsForCurrentWorkspace.map(tp => (
          <button
            key={tp.id}
            onClick={() => selectTeamProject(tp)}
            className={`w-full text-left px-3 py-1.5 rounded-md text-sm font-medium flex items-center justify-between group
              ${currentTeamProject?.id === tp.id && !currentChatRoom ? 'bg-primary-light text-primary-dark' : 'text-neutral-700 hover:bg-neutral-200'}`}
          >
            <span className="truncate flex items-center">
              <HashtagIcon className="w-4 h-4 mr-1 text-neutral-400 group-hover:text-neutral-600"/>
              {tp.name}
            </span>
          </button>
        ))}
         <button 
            className="w-full mt-1 text-sm text-primary hover:underline py-2 border border-primary-light rounded-md hover:bg-primary-light/10"
            onClick={() => setIsTeamActionModalOpen(true)}
        >
            새로운 팀 만들기/참여
        </button>
      </div>

      <div className="mt-3 pt-3 border-t border-neutral-200">
          <div className="flex justify-between items-center px-2 mb-0.5">
            <h3 className="text-xs font-semibold text-neutral-500 uppercase tracking-wider">그룹 채팅</h3>
            <button onClick={() => setIsNewChatModalOpen(true)} className="text-primary-dark hover:text-primary" title="새로운 채팅">
                <PlusCircleIcon className="w-4 h-4"/>
            </button>
          </div>
          {groupChats.map(room => (
             <div key={room.id} className="flex items-center group">
                <button
                    onClick={() => selectChatRoom(room.id)}
                    className={`flex-grow text-left px-3 py-1.5 rounded-l-md text-sm flex items-center truncate
                    ${currentChatRoom?.id === room.id ? 'bg-primary-light text-primary-dark font-semibold' : 'text-neutral-600 hover:bg-neutral-200'}`}
                >
                    <UserGroupIcon className="w-4 h-4 mr-1.5 text-neutral-400 group-hover:text-neutral-600"/>
                    {getChatRoomName(room, currentUser)}
                </button>
                <button 
                    onClick={() => handleDeleteChatRoom(room.id, getChatRoomName(room,currentUser))}
                    className={`p-1.5 rounded-r-md opacity-0 group-hover:opacity-100 hover:bg-red-100
                                ${currentChatRoom?.id === room.id ? 'bg-primary-light text-primary-dark' : 'text-neutral-600 hover:bg-neutral-200'}`}
                    title="채팅방 삭제/나가기"
                >
                    <ComponentTrashIcon className="w-3.5 h-3.5 text-red-500"/>
                </button>
            </div>
          ))}
      </div>
      
      <div className="mt-3 pt-3 border-t border-neutral-200">
          <div className="flex justify-between items-center px-2 mb-0.5">
            <h3 className="text-xs font-semibold text-neutral-500 uppercase tracking-wider">개인 메시지</h3>
          </div>
          {directMessages.map(room => (
             <div key={room.id} className="flex items-center group">
                <button
                    onClick={() => selectChatRoom(room.id)}
                    className={`flex-grow text-left px-3 py-1.5 rounded-l-md text-sm flex items-center truncate
                    ${currentChatRoom?.id === room.id ? 'bg-primary-light text-primary-dark font-semibold' : 'text-neutral-600 hover:bg-neutral-200'}`}
                >
                <img 
                    src={room.members.find(m => m.id !== currentUser.id)?.profilePictureUrl || `https://picsum.photos/seed/${room.members.find(m => m.id !== currentUser.id)?.id}/20/20`}
                    alt="dm partner"
                    className="w-4 h-4 rounded-full mr-1.5"
                />
                    {getChatRoomName(room, currentUser)}
                </button>
                <button 
                    onClick={() => handleDeleteChatRoom(room.id, getChatRoomName(room,currentUser))}
                     className={`p-1.5 rounded-r-md opacity-0 group-hover:opacity-100 hover:bg-red-100
                                ${currentChatRoom?.id === room.id ? 'bg-primary-light text-primary-dark' : 'text-neutral-600 hover:bg-neutral-200'}`}
                    title="대화 나가기"
                >
                    <ComponentTrashIcon className="w-3.5 h-3.5 text-red-500"/>
                </button>
            </div>
          ))}
           {(directMessages.length === 0 && groupChats.length === 0 && (
             <p className="text-xs text-neutral-500 px-3 py-2">채팅방이 없습니다. '새로운 채팅' 버튼으로 시작해보세요.</p>
           ))}
      </div>

      <div className="mt-3 pt-3 border-t border-neutral-200">
        <div className="flex justify-between items-center px-2 mb-0.5">
          <h3 className="text-xs font-semibold text-neutral-500 uppercase tracking-wider">화상회의 채널</h3>
        </div>
        {mockVideoRooms.map(room => (
            <div key={room.id} className="flex items-center group">
                <button
                    onClick={() => selectVideoRoom(room.name)}
                    className="flex-grow text-left px-3 py-1.5 rounded-l-md text-sm flex items-center truncate text-neutral-600 hover:bg-neutral-200"
                >
                    <VideoCameraIcon className="w-4 h-4 mr-1.5 text-neutral-400 group-hover:text-neutral-600"/>
                    {room.name}
                </button>
                 <button 
                    onClick={() => handleDeleteVideoRoom(room.id, room.name)}
                    className="p-1.5 rounded-r-md opacity-0 group-hover:opacity-100 hover:bg-red-100 text-neutral-600 hover:bg-neutral-200"
                    title="화상회의 채널 삭제"
                >
                    <ComponentTrashIcon className="w-3.5 h-3.5 text-red-500"/>
                </button>
            </div>
        ))}
        <Button 
            onClick={() => setIsNewVideoConferenceModalOpen(true)}
            variant="outline"
            className="w-full text-sm py-1.5 flex items-center justify-center space-x-1.5 mt-1"
        >
            <VideoCameraIcon className="w-4 h-4"/>
            <span>새 화상회의 시작</span>
        </Button>
      </div>

      <NewChatModal isOpen={isNewChatModalOpen} onClose={() => setIsNewChatModalOpen(false)} />
      <NewVideoConferenceModal isOpen={isNewVideoConferenceModalOpen} onClose={() => setIsNewVideoConferenceModalOpen(false)} />
      <TeamActionModal isOpen={isTeamActionModalOpen} onClose={() => setIsTeamActionModalOpen(false)} />
      <WorkspaceSettingsModal isOpen={isWorkspaceSettingsModalOpen} onClose={() => setIsWorkspaceSettingsModalOpen(false)} />

      {confirmDeleteInfo && (
        <Modal
            isOpen={!!confirmDeleteInfo}
            onClose={() => setConfirmDeleteInfo(null)}
            title={`${confirmDeleteInfo.type === 'chat' ? '채팅방' : '화상회의 채널'} 삭제 확인`}
            footer={
                <div className="flex justify-end space-x-2">
                    <Button variant="ghost" onClick={() => setConfirmDeleteInfo(null)}>취소</Button>
                    <Button variant="danger" onClick={confirmDeletion}>삭제</Button>
                </div>
            }
        >
            <p>정말로 <strong className="font-semibold">{confirmDeleteInfo.name}</strong> 
            {confirmDeleteInfo.type === 'chat' ? ' 채팅방을 삭제(퇴장)하시겠습니까?' : ' 화상회의 채널을 삭제하시겠습니까?'}
            <br/>이 작업은 되돌릴 수 없습니다. (목업)</p>
        </Modal>
      )}
    </aside>
  );
};


const AppLayout: React.FC = () => {
  const { currentWorkspace } = useAuth();
  const showSidebars = !!currentWorkspace;

  return (
    <div className="flex flex-col min-h-screen">
      <GlobalHeader />
      <div className="flex flex-grow pt-16"> 
        {showSidebars && <WorkspaceSidebar />}
        {showSidebars && <TeamProjectSidebar />}
        <main className={`flex-grow transition-all duration-300 ${showSidebars ? 'ml-16 md:ml-80' : 'ml-0'}`}>
          <div className="p-4 sm:p-6 lg:p-8">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};


function App() {
  return (
    <AuthProvider>
      <HashRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          
          <Route element={<ProtectedRoute />}> 
            <Route path="/ws/:workspaceId" element={<HomePage />} />
            <Route path="/ws/:workspaceId/team/:teamProjectId" element={<TeamSpacePage />} />
            <Route path="/ws/:workspaceId/chat/:roomId" element={<ChatPage />} />
            <Route path="/ws/:workspaceId/video/live" element={<VideoConferencePage />} />

            <Route path="/my-page" element={<MyPage />} />
            <Route path="/my-page/profile-edit" element={<ProfileEditPage />} />
            <Route path="/my-page/account-settings" element={<AccountSettingsPage />} />
            <Route path="/users/:userId" element={<UserProfileViewPage />} />
            
            <Route path="/team-formation" element={<TeamFormationHubPage />} />
            <Route path="/team-formation/auto-dice" element={<AutoTeamPage mode="dice" />} />
            <Route path="/team-formation/auto-ladder" element={<AutoTeamPage mode="ladder" />} />
            <Route path="/team-formation/vote" element={<VoteTeamPage />} />
            <Route path="/team-formation/admin" element={<AdminTeamPage />} />
            
            <Route path="/notifications" element={<NotificationsPage />} />
            
            <Route path="/" element={<NavigateToInitialView />} />
          </Route>
          <Route path="*" element={<Navigate to="/" />} /> 
        </Routes>
      </HashRouter>
    </AuthProvider>
  );
}

const NavigateToInitialView: React.FC = () => {
    const { isAuthenticated, currentUser } = useAuth();
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    const defaultWorkspaceId = currentUser?.currentWorkspaceId || MOCK_WORKSPACES_APP[0]?.id || 'ws_kosta';
    return <Navigate to={`/ws/${defaultWorkspaceId}`} replace />;
};


export default App;
