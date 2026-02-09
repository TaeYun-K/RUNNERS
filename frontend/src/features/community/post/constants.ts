import type { CommunityPostBoardType } from './types'

export const COMMUNITY_BOARD_LABEL: Record<CommunityPostBoardType, string> = {
  FREE: '자유',
  QNA: '질문',
  INFO: '정보',
}

export const COMMUNITY_BOARD_BADGE_CLASS: Record<
  CommunityPostBoardType,
  string
> = {
  FREE: 'bg-blue-100 text-blue-700 ring-blue-200',
  QNA: 'bg-yellow-100 text-yellow-800 ring-yellow-200',
  INFO: 'bg-green-100 text-green-700 ring-green-200',
}
