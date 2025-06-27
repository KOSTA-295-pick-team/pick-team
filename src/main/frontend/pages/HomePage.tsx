
import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { Card, Button, ProfileSummaryCard } from '../components';
import { TeamProject, User, Workspace } from '../types'; // Updated type import

// Mock data - in a real app, this would come from an API or a more robust state management
const MOCK_TEAM_PROJECTS_ALL: TeamProject[] = [
  { id: 'tp_alpha', workspaceId: 'ws_kosta', name: '알파 프로젝트 팀', members: [{id: '1', email:'a@a.com', name:'김알파'}, {id: '2', email:'b@b.com', name:'박알파'}], announcements: [{id:'a1', content:'회의공지', author:'김알파', timestamp:new Date()}], memberCount: 2, progress: 75 },
  { id: 'tp_beta', workspaceId: 'ws_kosta', name: '베타 서비스 개발팀', members: [{id: '3', email:'c@c.com', name:'이베타'}], announcements: [], memberCount: 1, progress: 40 },
  { id: 'tp_personal', workspaceId: 'ws_kosta', name: '개인프로젝트 팀', members: [], announcements: [], memberCount: 2, progress: 90 },

  { id: 'tp_project_x', workspaceId: 'ws_da', name: 'Project X', members: [], announcements: [], memberCount: 3, progress: 50 },
  { id: 'tp_idea_lab', workspaceId: 'ws_da', name: '아이디어 실험실', members: [], announcements: [], memberCount: 1, progress: 20 },
  { id: 'tp_research', workspaceId: 'ws_gaein', name: '개인 연구', members: [], announcements: [], memberCount: 1, progress: 10 },
];

const MOCK_WORKSPACES_ALL: Workspace[] = [
  { id: 'ws_kosta', name: 'kosta 2957', ownerId: 'user1', iconUrl: 'K', members: [] },
  { id: 'ws_da', name: '다목적 공간', ownerId: 'user1', iconUrl: '다', members: [] },
  { id: 'ws_gaein', name: '개인용', ownerId: 'user1', iconUrl: '개', members: [] },
];


const mockSuggestedUsers: User[] = [
    { id: 'user_park', name: '박해커', email: 'park@example.com', mbti: 'ISTP', tags: ['#개발', '#문제해결'], profilePictureUrl: 'https://picsum.photos/seed/userB/100/100' },
    { id: 'user_lee', name: '이디자인', email: 'lee@example.com', mbti: 'ISFP', tags: ['#디자인', '#UIUX'], profilePictureUrl: 'https://picsum.photos/seed/userC/100/100' },
    { id: 'user_choi', name: '최기획', email: 'choi@example.com', mbti: 'ENFJ', tags: ['#기획', '#PM'], profilePictureUrl: 'https://picsum.photos/seed/userD/100/100' },
];


export const HomePage: React.FC = () => {
  const { currentUser, currentWorkspace, setCurrentWorkspace, setCurrentTeamProject } = useAuth();
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const navigate = useNavigate();

  useEffect(() => {
    if (workspaceId && (!currentWorkspace || currentWorkspace.id !== workspaceId)) {
      const targetWs = MOCK_WORKSPACES_ALL.find(ws => ws.id === workspaceId);
      if (targetWs) {
        setCurrentWorkspace(targetWs);
      } else {
        // Handle workspace not found, maybe redirect to a default or error page
        navigate(`/ws/${MOCK_WORKSPACES_ALL[0].id}`); // Fallback to first workspace
      }
    }
    // Clear selected team project when navigating to a workspace home
    setCurrentTeamProject(null);
  }, [workspaceId, currentWorkspace, setCurrentWorkspace, navigate, setCurrentTeamProject]);

  if (!currentUser || !currentWorkspace) {
    // This should ideally be handled by ProtectedRoute and App.tsx's NavigateToInitialView
    // or show a loading state until context is ready.
    return <div className="p-4">워크스페이스 정보를 불러오는 중...</div>;
  }
  
  const teamsForCurrentWorkspace = MOCK_TEAM_PROJECTS_ALL.filter(tp => tp.workspaceId === currentWorkspace.id && !tp.name.startsWith('#'));


  return (
    <div className="space-y-8">
      <Card>
        <h1 className="text-2xl font-bold text-neutral-800">안녕하세요, {currentUser.name || '사용자'}님!</h1>
        <p className="text-neutral-600 mt-1">{currentWorkspace.name} 워크스페이스입니다.</p>
        <p className="text-neutral-600 mt-2">오늘도 팀플메이트와 함께 성공적인 프로젝트를 만들어보세요.</p>
      </Card>

      {/* My Teams Section */}
      <Card title="내 팀 목록">
        {teamsForCurrentWorkspace.length > 0 ? (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {teamsForCurrentWorkspace.map(team => (
              <Card key={team.id} className="bg-white hover:shadow-xl transition-shadow flex flex-col justify-between">
                <div>
                  <h3 className="text-lg font-semibold text-primary-dark">{team.name}</h3>
                  <p className="text-sm text-neutral-600 mt-1 mb-1">{team.memberCount || team.members.length}명의 팀원</p>
                  {team.progress !== undefined && (
                    <div className="my-2">
                      <div className="flex justify-between text-xs text-neutral-500 mb-0.5">
                        <span>프로젝트 진행도</span>
                        <span>{team.progress}%</span>
                      </div>
                      <div className="w-full bg-neutral-200 rounded-full h-1.5">
                        <div className="bg-primary h-1.5 rounded-full" style={{ width: `${team.progress}%` }}></div>
                      </div>
                    </div>
                  )}
                </div>
                <Link to={`/ws/${currentWorkspace.id}/team/${team.id}`} className="mt-4 block">
                  <Button variant="primary" size="sm" className="w-full">팀 스페이스 입장</Button>
                </Link>
              </Card>
            ))}
          </div>
        ) : (
          <p className="text-neutral-500 py-4 text-center">이 워크스페이스에 아직 팀/프로젝트가 없습니다.</p>
        )}
        <div className="mt-6 text-center">
            <Button 
                variant="outline" 
                size="lg" 
                onClick={() => alert(`'${currentWorkspace.name}'에서 새로운 팀 만들기 (목업)`)} // Or navigate to a creation page
            >
                새로운 팀 만들기 또는 참여하기
            </Button>
        </div>
      </Card>
      
      {/* Quick Actions - These might be more global or less prominent in a Slack-like UI */}
      {/* 
      <Card title="빠른 작업">
        ...
      </Card> 
      */}

      <Card title="함께할 팀원 찾아보기 (워크스페이스 내 추천)">
         <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {mockSuggestedUsers.filter(u => u.id !== currentUser.id).slice(0,3).map(user => (
                <ProfileSummaryCard key={user.id} user={user} />
            ))}
         </div>
         {mockSuggestedUsers.length === 0 && <p className="text-neutral-500 py-4 text-center">추천할 팀원이 없습니다.</p>}
      </Card>
    </div>
  );
};
