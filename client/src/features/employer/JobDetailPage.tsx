import { useParams } from 'react-router-dom'

export default function EmployerJobDetailPage() {
  const { id } = useParams()
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="text-center">
        <h1 className="text-2xl font-bold">Chi tiết việc làm</h1>
        <p className="mt-2 text-muted-foreground">Mã việc làm: {id}</p>
        <p className="text-muted-foreground">Đang phát triển</p>
      </div>
    </div>
  )
}
