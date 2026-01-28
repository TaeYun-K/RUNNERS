"use client";

import { Link } from "react-router-dom";
import { ChevronRight, Play, Instagram, Twitter, Github } from "lucide-react";

interface FooterCTAProps {
  isLoggedIn: boolean;
}

export function FooterCTA({ isLoggedIn }: FooterCTAProps) {
  const PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.runners.app";

  return (
    <footer className="relative overflow-hidden bg-card">
      {/* CTA Section */}
      <div className="relative border-b border-border">
        {/* Background Elements */}
        <div className="absolute inset-0">
          <div className="absolute left-1/4 top-0 h-[400px] w-[400px] rounded-full bg-blue-500/10 blur-[100px]" />
          <div className="absolute bottom-0 right-1/4 h-[300px] w-[300px] rounded-full bg-cyan-500/10 blur-[80px]" />
        </div>

        <div className="relative mx-auto max-w-7xl px-6 py-24 text-center lg:px-8 lg:py-32">
          <h2 className="text-balance text-3xl font-black leading-tight md:text-4xl lg:text-5xl">
            당신의 신발 끈을
            <br />
            <span className="bg-gradient-to-r from-blue-600 via-blue-500 to-cyan-500 bg-clip-text text-transparent">
              묶을 시간입니다
            </span>
          </h2>
          <p className="mx-auto mt-6 max-w-xl text-lg text-muted-foreground">
            지금 바로 RUNNERS와 함께 러닝을 시작하세요.
            <br />
            첫 번째 km가 당신의 여정을 바꿀 거예요.
          </p>

          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <a
              href={PLAY_STORE_URL}
              target="_blank"
              rel="noreferrer"
              className="group inline-flex w-full items-center justify-center gap-3 rounded-full bg-blue-600 px-8 py-4 text-base font-bold text-white transition-all hover:scale-[1.02] hover:bg-blue-700 hover:shadow-lg hover:shadow-blue-500/30 sm:w-auto"
            >
              <Play className="h-5 w-5 fill-current" />
              앱 다운로드
            </a>

            <Link
              to={isLoggedIn ? "/dashboard" : "/login"}
              className="group inline-flex w-full items-center justify-center gap-2 rounded-full border border-border bg-secondary/50 px-8 py-4 text-base font-semibold text-foreground backdrop-blur-sm transition-all hover:border-blue-500/50 hover:bg-secondary sm:w-auto"
            >
              {isLoggedIn ? "대시보드로 이동" : "웹에서 시작하기"}
              <ChevronRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
            </Link>
          </div>
        </div>
      </div>

      {/* Footer Bottom */}
      <div className="mx-auto max-w-7xl px-6 py-12 lg:px-8">
        <div className="flex flex-col items-center justify-between gap-8 md:flex-row">
          {/* Logo & Copyright */}
          <div className="flex flex-col items-center gap-4 md:items-start">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-500/20">
                <span className="text-lg font-black text-blue-600">R</span>
              </div>
              <span className="text-xl font-bold text-foreground">RUNNERS</span>
            </div>
            <p className="text-sm text-muted-foreground">
              © 2026 RUNNERS. All rights reserved.
            </p>
          </div>

          {/* Links */}
          <div className="flex flex-wrap items-center justify-center gap-8 text-sm">
            <Link
              to="/terms"
              className="text-muted-foreground transition-colors hover:text-foreground"
            >
              이용약관
            </Link>
            <Link
              to="/privacy"
              className="text-muted-foreground transition-colors hover:text-foreground"
            >
              개인정보처리방침
            </Link>
            <Link
              to="/support"
              className="text-muted-foreground transition-colors hover:text-foreground"
            >
              고객지원
            </Link>
          </div>

          {/* Social Links */}
          <div className="flex items-center gap-4">
            <a
              href="https://instagram.com"
              target="_blank"
              rel="noreferrer"
              className="flex h-10 w-10 items-center justify-center rounded-full border border-border transition-all hover:border-blue-500/50 hover:bg-secondary"
              aria-label="Instagram"
            >
              <Instagram className="h-5 w-5 text-muted-foreground" />
            </a>
            <a
              href="https://twitter.com"
              target="_blank"
              rel="noreferrer"
              className="flex h-10 w-10 items-center justify-center rounded-full border border-border transition-all hover:border-blue-500/50 hover:bg-secondary"
              aria-label="Twitter"
            >
              <Twitter className="h-5 w-5 text-muted-foreground" />
            </a>
            <a
              href="https://github.com"
              target="_blank"
              rel="noreferrer"
              className="flex h-10 w-10 items-center justify-center rounded-full border border-border transition-all hover:border-blue-500/50 hover:bg-secondary"
              aria-label="GitHub"
            >
              <Github className="h-5 w-5 text-muted-foreground" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
