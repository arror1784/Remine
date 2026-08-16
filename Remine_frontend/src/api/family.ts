import { http, type ApiEnvelope, unwrap } from '@/api/http'

export interface UserResponse {
  id: string
  role: 'PARENT' | 'CHILD'
  name: string
  ageGroup: string
  interests: string[]
  email: string | null
  inviteCode: string | null
  pairedUserId: string | null
  streakDays: number
  createdAt: string
  updatedAt: string
}

// Creating a post returns the bare entity; only the feed carries viewer state.
export interface CreatedPost {
  id: string
  authorUserId: string
  body: string
  photoUrl: string | null
  photoCaption: string | null
  likeCount: number
  createdAt: string
  updatedAt: string
}

export interface Post extends CreatedPost {
  likedByViewer: boolean
  replyCount: number
}

export interface Reply {
  id: string
  postId: string
  authorUserId: string
  body: string
  createdAt: string
  updatedAt: string
}

export interface FamilySummary {
  sharedPhotoCount: number
  quizTogetherCount: number
  messageCount: number
  callCount: number
}

export async function getMyProfile(): Promise<UserResponse> {
  const response = await http.get<ApiEnvelope<UserResponse>>('/api/v1/users/me')
  return unwrap(response.data)
}

// A null `data` is a valid answer here (not yet paired), so this skips `unwrap`.
export async function getPairedProfile(): Promise<UserResponse | null> {
  const response = await http.get<ApiEnvelope<UserResponse>>('/api/v1/users/me/paired')
  if (response.data.error) throw new Error(response.data.error.message)
  return response.data.data
}

export async function listPosts(cursor?: string): Promise<Post[]> {
  const response = await http.get<ApiEnvelope<Post[]>>('/api/v1/family/posts', {
    params: { cursor, limit: 20 },
  })
  return unwrap(response.data)
}

export async function createPost(body: string, photoUrl?: string, photoCaption?: string): Promise<CreatedPost> {
  const response = await http.post<ApiEnvelope<CreatedPost>>('/api/v1/family/posts', {
    body,
    photoUrl,
    photoCaption,
  })
  return unwrap(response.data)
}

export async function toggleLike(postId: string): Promise<{ liked: boolean; likeCount: number }> {
  const response = await http.patch<ApiEnvelope<{ liked: boolean; likeCount: number }>>(
    `/api/v1/family/posts/${postId}/like`
  )
  return unwrap(response.data)
}

export async function listReplies(postId: string): Promise<Reply[]> {
  const response = await http.get<ApiEnvelope<Reply[]>>(`/api/v1/family/posts/${postId}/replies`)
  return unwrap(response.data)
}

export async function createReply(postId: string, body: string): Promise<Reply> {
  const response = await http.post<ApiEnvelope<Reply>>(`/api/v1/family/posts/${postId}/replies`, { body })
  return unwrap(response.data)
}

export async function getFamilySummary(): Promise<FamilySummary> {
  const response = await http.get<ApiEnvelope<FamilySummary>>('/api/v1/family/summary')
  return unwrap(response.data)
}
