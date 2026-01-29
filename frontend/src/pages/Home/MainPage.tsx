'use client'

import { useAuth } from '../../features/auth'
import { HeroSection } from './components/hero-section'
import { FeaturesSection } from './components/features-section'
import { FooterCTA } from './components/footer-cta'

export default function MainPage() {
  const { accessToken } = useAuth()
  const isLoggedIn = Boolean(accessToken)

  return (
    <div className="min-h-screen bg-background text-foreground">
      <HeroSection isLoggedIn={isLoggedIn} />
      <FeaturesSection />
      <FooterCTA isLoggedIn={isLoggedIn} />
    </div>
  )
}
