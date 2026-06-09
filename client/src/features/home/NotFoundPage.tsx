import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold">404</h1>
        <p className="mt-2 text-muted-foreground">Không tìm thấy trang</p>
        <Link to="/" className="mt-4 inline-block text-sm text-primary hover:underline">
          Về trang chủ
        </Link>
      </div>
    </div>
  )
}
