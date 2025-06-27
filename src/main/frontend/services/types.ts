// API 응답 타입 정의
export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface PostResponse {
  id: number;
  postNo: number;
  title: string;
  content: string;
  authorName: string;
  authorId: number;
  boardId: number;
  createdAt: string;
  updatedAt: string;
  attachments: PostAttachResponse[];
  commentCount: number;
}

export interface PostAttachResponse {
  id: number;
  originalName: string;
  hashedName: string;
  fileSize: number;
}

export interface CommentResponse {
  id: number;
  content: string;
  authorName: string;
  authorId: number;
  postId: number;
  createdAt: string;
  updatedAt: string;
}

export interface ScheduleResponse {
  id: number;
  title: string;
  startDate: string;
  endDate: string;
  scheduleDesc?: string;
  type: "MEETING" | "DEADLINE" | "WORKSHOP" | "VACATION" | "OTHER";
  typeName: string;
  creatorName: string;
  creatorId: number;
  teamName: string;
  teamId: number;
  createdAt: string;
  updatedAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface PostCreateRequest {
  title: string;
  content: string;
  boardId: number;
  attachmentIds?: number[]; // 첨부파일 ID 배열 추가
}

export interface PostUpdateRequest {
  title: string;
  content: string;
}

export interface CommentCreateRequest {
  content: string;
}

export interface CommentUpdateRequest {
  content: string;
}

export interface ScheduleCreateRequest {
  title: string;
  startDate: string;
  endDate: string;
  scheduleDesc?: string;
  type: string;
}

export interface ScheduleUpdateRequest {
  title: string;
  startDate: string;
  endDate: string;
  scheduleDesc?: string;
  type: string;
}

// 파일 업로드 관련 타입
export interface FileUploadResponse {
  id: number;
  originalName: string;
  hashedName: string;
  fileSize: number;
}

export interface FileDownloadInfo {
  fileName: string;
  fileUrl: string;
}
