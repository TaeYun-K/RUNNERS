import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="card">
      <h2>페이지를 찾을 수 없어요</h2>
      <Link to="/" className="btn btn-primary">
        홈으로
      </Link>
    </section>
  )
}

