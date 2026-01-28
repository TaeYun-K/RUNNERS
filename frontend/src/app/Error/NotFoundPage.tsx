import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="mx-auto max-w-lg rounded-2xl border border-border bg-card p-6 text-center shadow-sm">
      <h2 className="text-xl font-bold tracking-tight text-foreground">
        페이지를 찾을 수 없어요
      </h2>
      <Link
        to="/"
        className="mt-4 inline-flex h-11 items-center justify-center rounded-xl bg-blue-600 px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
      >
        홈으로
      </Link>
    </section>
  )
}
