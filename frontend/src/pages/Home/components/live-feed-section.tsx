"use client";

import { useState } from "react";
import { Link } from "react-router-dom";
import { ChevronRight, MapPin, Heart, MessageCircle } from "lucide-react";

interface Post {
  id: number;
  title: string;
  distance: number;
  authorName: string;
  authorLevel: string;
  timeAgo: string;
  likes: number;
  comments: number;
  image?: string;
}

// Mock data for demonstration
const mockPosts: Post[] = [
  {
    id: 1,
    title: "한강 선셋 러닝 🌅",
    distance: 7.2,
    authorName: "러닝맨_김철수",
    authorLevel: "골드",
    timeAgo: "30분 전",
    likes: 24,
    comments: 5,
  },
  {
    id: 2,
    title: "첫 10km 완주!",
    distance: 10.1,
    authorName: "초보러너_민지",
    authorLevel: "실버",
    timeAgo: "1시간 전",
    likes: 89,
    comments: 23,
  },
  {
    id: 3,
    title: "새벽 5시 러닝 루틴",
    distance: 5.5,
    authorName: "모닝러너",
    authorLevel: "다이아",
    timeAgo: "2시간 전",
    likes: 156,
    comments: 31,
  },
  {
    id: 4,
    title: "비오는 날도 달려요",
    distance: 4.8,
    authorName: "레인러너",
    authorLevel: "플래티넘",
    timeAgo: "3시간 전",
    likes: 67,
    comments: 12,
  },
];

const levelColors: Record<string, string> = {
  브론즈: "bg-amber-100 text-amber-700",
  실버: "bg-slate-200 text-slate-600",
  골드: "bg-yellow-100 text-yellow-700",
  플래티넘: "bg-cyan-100 text-cyan-700",
  다이아: "bg-blue-100 text-blue-700",
};

export function LiveFeedSection() {
  const [posts] = useState<Post[]>(mockPosts);
  const isLoading = false;

  return (
    <section className="relative py-24 lg:py-32">
      {/* Background */}
      <div className="absolute inset-0 bg-background" />

      <div className="relative mx-auto max-w-7xl px-6 lg:px-8">
        {/* Section Header */}
        <div className="flex flex-col items-start justify-between gap-6 sm:flex-row sm:items-end">
          <div>
            <p className="text-sm font-semibold uppercase tracking-widest text-blue-600">
              Live Feed
            </p>
            <h2 className="mt-4 text-3xl font-black md:text-4xl">
              지금 이 순간에도
              <br />
              <span className="text-muted-foreground">러너들은 성장 중</span>
            </h2>
          </div>
          <Link
            to="/community"
            className="group inline-flex items-center gap-2 rounded-full border border-border bg-secondary/50 px-6 py-3 text-sm font-semibold transition-all hover:border-blue-500/50 hover:bg-secondary"
          >
            전체 피드 보기
            <ChevronRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
          </Link>
        </div>

        {/* Feed Grid */}
        <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {posts.map((post) => (
            <Link
              key={post.id}
              to={`/community/${post.id}`}
              className="group relative overflow-hidden rounded-3xl border border-border bg-card/50 transition-all duration-300 hover:border-blue-500/30 hover:bg-card/80 hover:shadow-lg hover:shadow-blue-500/5"
            >
              {/* Image Placeholder */}
              <div className="relative aspect-[4/3] overflow-hidden bg-secondary">
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <MapPin className="h-6 w-6" />
                    <span className="text-sm font-medium uppercase tracking-wider">
                      Running Route
                    </span>
                  </div>
                </div>

                {/* Distance Badge */}
                <div className="absolute right-3 top-3 rounded-full bg-background/90 px-3 py-1.5 backdrop-blur-sm">
                  <span className="text-sm font-bold text-blue-600">
                    {post.distance.toFixed(1)} km
                  </span>
                </div>

                {/* Gradient Overlay */}
                <div className="absolute inset-0 bg-gradient-to-t from-card via-transparent to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100" />
              </div>

              {/* Content */}
              <div className="p-5">
                <h3 className="line-clamp-1 text-lg font-bold text-foreground transition-colors group-hover:text-blue-600">
                  {post.title}
                </h3>

                {/* Author Info */}
                <div className="mt-4 flex items-center gap-3">
                  <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-secondary to-muted">
                    <span className="text-xs font-bold text-muted-foreground">
                      {post.authorName.charAt(0)}
                    </span>
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-foreground">
                      {post.authorName}
                    </p>
                    <div className="flex items-center gap-2">
                      <span
                        className={`rounded px-1.5 py-0.5 text-xs font-medium ${levelColors[post.authorLevel] || "bg-secondary text-muted-foreground"}`}
                      >
                        {post.authorLevel}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {post.timeAgo}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Engagement */}
                <div className="mt-4 flex items-center gap-4 border-t border-border pt-4">
                  <div className="flex items-center gap-1.5 text-muted-foreground">
                    <Heart className="h-4 w-4" />
                    <span className="text-sm">{post.likes}</span>
                  </div>
                  <div className="flex items-center gap-1.5 text-muted-foreground">
                    <MessageCircle className="h-4 w-4" />
                    <span className="text-sm">{post.comments}</span>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>

        {/* Empty State */}
        {posts.length === 0 && !isLoading && (
          <div className="mt-12 rounded-3xl border-2 border-dashed border-border py-20 text-center">
            <MapPin className="mx-auto h-12 w-12 text-muted-foreground/50" />
            <p className="mt-4 text-lg font-medium text-muted-foreground">
              아직 활동 중인 러너가 없습니다
            </p>
            <p className="mt-1 text-sm text-muted-foreground/70">
              첫 번째 러닝을 기록해보세요!
            </p>
          </div>
        )}
      </div>
    </section>
  );
}
