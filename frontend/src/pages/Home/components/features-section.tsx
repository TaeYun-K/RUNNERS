"use client";

import { BarChart3, Users, Zap, Trophy, Target, Medal } from "lucide-react";

const features = [
  {
    icon: BarChart3,
    title: "자동 대시보드",
    description:
      "파편화된 러닝 기록을 한눈에. AI가 자동으로 분석하고 인사이트를 제공합니다.",
    color: "text-blue-500",
    bgColor: "bg-blue-500/10",
  },
  {
    icon: Zap,
    title: "레벨업 시스템",
    description:
      "달린 거리만큼 경험치 획득. 브론즈부터 다이아몬드까지, 성장의 즐거움을 느끼세요.",
    color: "text-blue-500",
    bgColor: "bg-blue-500/10",
  },
  {
    icon: Users,
    title: "러너스 커뮤니티",
    description:
      "혼자가 아닌 함께. 전국의 러너들과 기록을 공유하고 응원을 주고받으세요.",
    color: "text-green-500",
    bgColor: "bg-green-500/10",
  },
  {
    icon: Trophy,
    title: "뱃지 & 챌린지",
    description:
      "목표 달성마다 특별한 뱃지를 수집하세요. 주간 챌린지로 동기부여를 유지하세요.",
    color: "text-pink-500",
    bgColor: "bg-pink-500/10",
  },
  {
    icon: Target,
    title: "맞춤형 목표 설정",
    description:
      "나만의 러닝 목표를 설정하고 달성률을 실시간으로 확인하세요.",
    color: "text-cyan-500",
    bgColor: "bg-cyan-500/10",
  },
  {
    icon: Medal,
    title: "월간 랭킹",
    description:
      "지역별, 전체 랭킹에서 나의 위치를 확인하고 더 높은 곳을 향해 도전하세요.",
    color: "text-orange-500",
    bgColor: "bg-orange-500/10",
  },
];

export function FeaturesSection() {
  return (
    <section
      id="features"
      className="relative overflow-hidden bg-secondary/30 py-24 lg:py-32"
    >
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-[0.02]">
        <div
          className="absolute inset-0"
          style={{
            backgroundImage: `radial-gradient(circle at 1px 1px, currentColor 1px, transparent 1px)`,
            backgroundSize: "40px 40px",
          }}
        />
      </div>

      <div className="relative mx-auto max-w-7xl px-6 lg:px-8">
        {/* Section Header */}
        <div className="mx-auto max-w-3xl text-center">
          <p className="text-sm font-semibold uppercase tracking-widest text-blue-600">
            Features
          </p>
          <h2 className="mt-4 text-balance text-3xl font-black leading-tight md:text-4xl lg:text-5xl">
            왜 러너들은{" "}
            <span className="bg-gradient-to-r from-blue-600 via-blue-500 to-cyan-500 bg-clip-text text-transparent">
              RUNNERS
            </span>
            를 선택할까요?
          </h2>
          <p className="mt-6 text-lg leading-relaxed text-muted-foreground">
            단순한 기록 앱을 넘어, 진짜 성장을 경험할 수 있는 러닝 플랫폼
          </p>
        </div>

        {/* Features Grid */}
        <div className="mt-16 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((feature) => (
            <div
              key={feature.title}
              className="group relative rounded-3xl border border-border bg-card/50 p-8 backdrop-blur-sm transition-all duration-300 hover:border-blue-500/30 hover:bg-card/80 hover:shadow-lg hover:shadow-blue-500/5"
            >
              {/* Icon */}
              <div
                className={`inline-flex h-14 w-14 items-center justify-center rounded-2xl ${feature.bgColor}`}
              >
                <feature.icon className={`h-7 w-7 ${feature.color}`} />
              </div>

              {/* Content */}
              <h3 className="mt-6 text-xl font-bold text-foreground">
                {feature.title}
              </h3>
              <p className="mt-3 leading-relaxed text-muted-foreground">
                {feature.description}
              </p>

              {/* Hover Gradient */}
              <div className="absolute inset-0 -z-10 rounded-3xl bg-gradient-to-br from-blue-500/5 via-transparent to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100" />
            </div>
          ))}
        </div>

        {/* Stats Bar */}
        <div className="mt-20 rounded-3xl border border-border bg-card/50 p-8 backdrop-blur-sm lg:p-12">
          <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
            {[
              { value: "10K+", label: "활성 러너" },
              { value: "500K+", label: "누적 러닝 km" },
              { value: "50K+", label: "공유된 기록" },
              { value: "4.8", label: "앱스토어 평점" },
            ].map((stat) => (
              <div key={stat.label} className="text-center">
                <p className="text-3xl font-black text-foreground md:text-4xl lg:text-5xl">
                  {stat.value}
                </p>
                <p className="mt-2 text-sm text-muted-foreground md:text-base">
                  {stat.label}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
