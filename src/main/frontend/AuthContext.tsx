import React, {
  createContext,
  useState,
  useContext,
  ReactNode,
  useCallback,
  useEffect,
} from "react";
import {
  User,
  Workspace,
  TeamProject,
  ChatRoom,
  ChatRoomMember,
  ChatMessage,
} from "./types";

interface AuthContextType {
  currentUser: User | null;
  login: (user: User) => void;
  logout: () => void;
  isAuthenticated: boolean;
  currentWorkspace: Workspace | null;
  setCurrentWorkspace: (workspace: Workspace | null) => void;
  currentTeamProject: TeamProject | null;
  setCurrentTeamProject: (teamProject: TeamProject | null) => void;
  updateUserProfile: (updatedProfileData: Partial<User>) => void;

  // Chat specific context
  chatRooms: ChatRoom[];
  currentChatRoom: ChatRoom | null;
  setCurrentChatRoomById: (roomId: string | null) => void;
  createChatRoom: (
    name: string | undefined,
    members: ChatRoomMember[],
    type: "dm" | "group"
  ) => Promise<ChatRoom | null>;
  deleteChatRoom: (roomId: string) => void; // Added for chat room deletion
  allUsersForChat: User[]; // For selecting users in chat creation
  getChatRoomName: (room: ChatRoom, currentUser: User) => string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const MOCK_WORKSPACES: Workspace[] = [
  {
    id: "ws_kosta",
    name: "kosta 2957",
    ownerId: "user@example.com",
    iconUrl: "K",
    members: [
      {
        id: "user@example.com",
        name: "테스트 사용자",
        profilePictureUrl: "https://picsum.photos/seed/user1/40/40",
      },
      {
        id: "user_kim",
        name: "김코딩",
        profilePictureUrl: "https://picsum.photos/seed/userA/40/40",
      },
      {
        id: "user_park",
        name: "박해커",
        profilePictureUrl: "https://picsum.photos/seed/userB/40/40",
      },
    ],
    inviteCode: "kosta2957-invite-xyz",
  },
  {
    id: "ws_da",
    name: "다목적 워크스페이스",
    ownerId: "user@example.com",
    iconUrl: "다",
    members: [
      {
        id: "user@example.com",
        name: "테스트 사용자",
        profilePictureUrl: "https://picsum.photos/seed/user1/40/40",
      },
      {
        id: "user_lee",
        name: "이디자인",
        profilePictureUrl: "https://picsum.photos/seed/userC/40/40",
      },
    ],
    inviteCode: "da-invite-abc",
  },
  {
    id: "ws_gaein",
    name: "개인 프로젝트용",
    ownerId: "user@example.com",
    iconUrl: "개",
    members: [
      {
        id: "user@example.com",
        name: "테스트 사용자",
        profilePictureUrl: "https://picsum.photos/seed/user1/40/40",
      },
    ],
    inviteCode: "gaein-invite-123",
  },
];

export const MOCK_USERS_FOR_CHAT: User[] = [
  {
    id: "user@example.com",
    email: "user@example.com",
    name: "테스트 사용자",
    profilePictureUrl: "https://picsum.photos/seed/user1/40/40",
  },
  {
    id: "user_kim",
    email: "kim@example.com",
    name: "김코딩",
    profilePictureUrl: "https://picsum.photos/seed/userA/40/40",
  },
  {
    id: "user_park",
    email: "park@example.com",
    name: "박해커",
    profilePictureUrl: "https://picsum.photos/seed/userB/40/40",
  },
  {
    id: "user_lee",
    email: "lee@example.com",
    name: "이디자인",
    profilePictureUrl: "https://picsum.photos/seed/userC/40/40",
  },
];

const MOCK_CHAT_ROOMS_INITIAL: ChatRoom[] = [
  {
    id: "chat_dm_user_kim",
    workspaceId: "ws_kosta",
    type: "dm",
    members: [
      {
        id: "user@example.com",
        name: "테스트 사용자",
        profilePictureUrl: "https://picsum.photos/seed/user1/40/40",
      },
      {
        id: "user_kim",
        name: "김코딩",
        profilePictureUrl: "https://picsum.photos/seed/userA/40/40",
      },
    ],
    lastMessage: {
      id: "dm_msg1",
      roomId: "chat_dm_user_kim",
      userId: "user_kim",
      userName: "김코딩",
      text: "안녕하세요! DM입니다.",
      timestamp: new Date(Date.now() - 3600000),
    },
    createdAt: new Date(Date.now() - 2 * 86400000),
    updatedAt: new Date(Date.now() - 3600000),
  },
  {
    id: "chat_group_general",
    workspaceId: "ws_kosta",
    name: "일반 토론방",
    type: "group",
    members: MOCK_USERS_FOR_CHAT.filter((u) => u.id !== "user_lee"), // Everyone except lee
    lastMessage: {
      id: "grp_msg1",
      roomId: "chat_group_general",
      userId: "user_park",
      userName: "박해커",
      text: "새로운 기능 아이디어 공유합니다.",
      timestamp: new Date(Date.now() - 7200000),
    },
    createdAt: new Date(Date.now() - 5 * 86400000),
    updatedAt: new Date(Date.now() - 7200000),
    creatorId: "user@example.com",
  },
  {
    id: "chat_group_project_alpha_discussion",
    workspaceId: "ws_kosta",
    name: "알파 프로젝트 논의",
    type: "group",
    members: [
      // Specific members for this group
      MOCK_USERS_FOR_CHAT.find((u) => u.id === "user@example.com")!,
      MOCK_USERS_FOR_CHAT.find((u) => u.id === "user_kim")!,
      MOCK_USERS_FOR_CHAT.find((u) => u.id === "user_park")!,
    ],
    createdAt: new Date(Date.now() - 3 * 86400000),
    updatedAt: new Date(Date.now() - 86400000),
    creatorId: "user_kim",
  },
];

export const AuthProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [currentWorkspace, _setCurrentWorkspace] = useState<Workspace | null>(
    null
  );
  const [currentTeamProject, _setCurrentTeamProject] =
    useState<TeamProject | null>(null);

  // Chat state
  const [chatRooms, setChatRooms] = useState<ChatRoom[]>(
    MOCK_CHAT_ROOMS_INITIAL
  );
  const [currentChatRoom, _setCurrentChatRoom] = useState<ChatRoom | null>(
    null
  );

  const login = useCallback((user: User) => {
    const mockUserWithPic: User = {
      ...user,
      id: user.id || "mockUserId",
      email: user.email,
      name: user.name || "Mock User",
      profilePictureUrl:
        user.profilePictureUrl ||
        `https://picsum.photos/seed/${user.email}/100/100`,
      mbti: user.mbti || "ISTP",
      tags: user.tags || ["#아침형인간", "#리더역할선호"],
      currentWorkspaceId: user.currentWorkspaceId || MOCK_WORKSPACES[0]?.id,
    };
    setCurrentUser(mockUserWithPic);
    if (mockUserWithPic.currentWorkspaceId) {
      const foundWorkspace = MOCK_WORKSPACES.find(
        (ws) => ws.id === mockUserWithPic.currentWorkspaceId
      );
      _setCurrentWorkspace(foundWorkspace || MOCK_WORKSPACES[0] || null);
    } else {
      _setCurrentWorkspace(MOCK_WORKSPACES[0] || null);
    }
    _setCurrentTeamProject(null);
    _setCurrentChatRoom(null); // Clear chat room on login
  }, []);

  const logout = useCallback(() => {
    setCurrentUser(null);
    _setCurrentWorkspace(null);
    _setCurrentTeamProject(null);
    _setCurrentChatRoom(null);
  }, []);

  const setCurrentWorkspaceInternal = useCallback(
    (workspace: Workspace | null) => {
      _setCurrentWorkspace(workspace);
      setCurrentUser((prevUser) =>
        prevUser ? { ...prevUser, currentWorkspaceId: workspace?.id } : null
      );
      _setCurrentTeamProject(null);
      _setCurrentChatRoom(null); // Reset chat room when workspace changes
    },
    []
  );

  const setCurrentTeamProjectInternal = useCallback(
    (teamProject: TeamProject | null) => {
      _setCurrentTeamProject(teamProject);
      setCurrentUser((prevUser) =>
        prevUser ? { ...prevUser, currentTeamProjectId: teamProject?.id } : null
      );
      _setCurrentChatRoom(null); // Also clear chat room if a team project is selected
    },
    []
  );

  const updateUserProfile = useCallback((updatedProfileData: Partial<User>) => {
    setCurrentUser((prevUser) => {
      if (!prevUser) return null;
      return { ...prevUser, ...updatedProfileData };
    });
  }, []);

  useEffect(() => {
    if (currentUser && currentUser.currentWorkspaceId && !currentWorkspace) {
      const foundWorkspace = MOCK_WORKSPACES.find(
        (ws) => ws.id === currentUser.currentWorkspaceId
      );
      _setCurrentWorkspace(foundWorkspace || MOCK_WORKSPACES[0] || null);
    } else if (
      currentUser &&
      !currentUser.currentWorkspaceId &&
      !currentWorkspace
    ) {
      _setCurrentWorkspace(MOCK_WORKSPACES[0] || null);
      setCurrentUser((prev) =>
        prev ? { ...prev, currentWorkspaceId: MOCK_WORKSPACES[0]?.id } : null
      );
    }
  }, [currentUser, currentWorkspace]);

  // Chat functions
  const setCurrentChatRoomById = useCallback(
    (roomId: string | null) => {
      if (!roomId) {
        _setCurrentChatRoom(null);
        // Potentially clear currentTeamProject if a chat room selection means we are not in a team project context
        // _setCurrentTeamProject(null);
        return;
      }
      const room = chatRooms.find(
        (r) => r.id === roomId && r.workspaceId === currentWorkspace?.id
      );
      _setCurrentChatRoom(room || null);
      // If a chat room is selected, clear team project selection as they are distinct views
      // _setCurrentTeamProject(null);
    },
    [chatRooms, currentWorkspace]
  );

  const createChatRoom = useCallback(
    async (
      name: string | undefined,
      members: ChatRoomMember[],
      type: "dm" | "group"
    ): Promise<ChatRoom | null> => {
      if (!currentUser || !currentWorkspace) return null;

      if (type === "dm") {
        // Ensure there are exactly two members for a DM, one of whom is the current user.
        const otherMember = members.find((m) => m.id !== currentUser.id);
        if (members.length !== 2 || !otherMember) {
          console.error("DM must have exactly two distinct members.");
          return null; // Or throw error
        }
        // Check if DM already exists
        const existingDm = chatRooms.find(
          (room) =>
            room.type === "dm" &&
            room.workspaceId === currentWorkspace.id &&
            room.members.length === 2 &&
            room.members.some((m) => m.id === currentUser.id) &&
            room.members.some((m) => m.id === otherMember.id)
        );
        if (existingDm) {
          return existingDm; // Return existing DM
        }
      } else {
        // Group chat
        if (!name || name.trim() === "") {
          console.error("Group chat must have a name.");
          return null;
        }
        if (members.length < 2) {
          // Allow group chat with self, but typically needs more. For now, require at least 2.
          console.error("Group chat must have at least 2 members.");
          return null;
        }
      }

      const newRoom: ChatRoom = {
        id: `chat_${type}_${Date.now()}`,
        workspaceId: currentWorkspace.id,
        name: type === "group" ? name : undefined,
        type,
        members,
        createdAt: new Date(),
        updatedAt: new Date(),
        creatorId: currentUser.id,
      };
      setChatRooms((prev) => [...prev, newRoom]);
      return newRoom;
    },
    [currentUser, currentWorkspace, chatRooms]
  );

  const deleteChatRoom = useCallback(
    (roomId: string) => {
      setChatRooms((prev) => prev.filter((room) => room.id !== roomId));
      if (currentChatRoom?.id === roomId) {
        _setCurrentChatRoom(null);
      }
      // Note: This is a mock deletion. In a real app, you'd call an API.
      // Also, consider implications: if it's a DM, does it delete for both users?
      // If it's a group, are you leaving or truly deleting (if admin)?
      // For this mock, we'll just remove it from the list.
    },
    [currentChatRoom]
  );

  const getChatRoomName = useCallback((room: ChatRoom, user: User): string => {
    if (room.type === "group") {
      return room.name || "Unnamed Group";
    }
    // For DM, find the other member's name
    const otherMember = room.members.find((m) => m.id !== user.id);
    return otherMember?.name || "DM";
  }, []);

  const isAuthenticated = !!currentUser;
  const allUsersForChat = MOCK_USERS_FOR_CHAT; // Provide all mock users for selection

  const filteredChatRooms = currentWorkspace
    ? chatRooms.filter((room) => room.workspaceId === currentWorkspace.id)
    : [];

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        login,
        logout,
        isAuthenticated,
        currentWorkspace,
        setCurrentWorkspace: setCurrentWorkspaceInternal,
        currentTeamProject,
        setCurrentTeamProject: setCurrentTeamProjectInternal,
        updateUserProfile,
        chatRooms: filteredChatRooms,
        currentChatRoom,
        setCurrentChatRoomById,
        createChatRoom,
        deleteChatRoom,
        allUsersForChat,
        getChatRoomName,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

// Helper function to get numeric accountId for API calls
export const getAccountId = (user: User | null): number => {
  if (!user) return 1; // 기본값
  // 실제 환경에서는 user.id를 숫자로 변환하거나 별도 필드 사용
  return user.id === "user@example.com"
    ? 1
    : user.id === "user_kim"
    ? 2
    : user.id === "user_park"
    ? 3
    : 1;
};
