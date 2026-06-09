import { useAuth } from '@/hooks/useAuth'

export function DashboardTopbar() {
  const { user } = useAuth()

  return (
    <header className="flex h-16 items-center justify-end border-b bg-card px-6">
      <div className="flex items-center gap-3">
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-600 text-sm font-semibold text-white">
          {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
        </div>
        <span className="text-sm font-medium">{user?.fullName}</span>
      </div>
    </header>
  )
}
