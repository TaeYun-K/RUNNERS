'use client';

import type { MyActivityMode } from '../../features/community/post'
import {
  CommunityMainSection,
  CommunityActivitySection,
} from '../../features/community/post/components/CommunityPostListSection'

type CommunityPageProps = {
  mode?: 'all' | MyActivityMode
}

export function CommunityPage({ mode = 'all' }: CommunityPageProps) {
  if (mode === 'all') {
    return <CommunityMainSection />
  }

  return <CommunityActivitySection mode={mode} />
}

