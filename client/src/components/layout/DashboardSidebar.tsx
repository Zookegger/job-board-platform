import { NavLink } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import type { LucideIcon } from 'lucide-react'
import { LogOut } from 'lucide-react'

interface NavItem {
  to: string
  label: string
  icon: LucideIcon
}

interface DashboardSidebarProps {
  navItems: NavItem[]
}

export function DashboardSidebar({ navItems }: DashboardSidebarProps) {
  const { user, logout } = useAuth()

  return (
    <aside className="flex h-screen w-64 flex-col border-r bg-card">
      <div className="flex h-16 items-center border-b px-6">
        <span className="text-xl font-bold text-primary">JobBoard</span>
      </div>

      <nav className="flex-1 space-y-1 p-4">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-primary/10 text-primary'
                  : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
              }`
            }
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="border-t p-4">
        <div className="mb-3 truncate text-sm font-medium">{user?.fullName}</div>
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
        >
          <LogOut className="h-4 w-4" />
          Đăng xuất
        </button>
      </div>
    </aside>
  )
}
