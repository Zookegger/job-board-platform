import { createBrowserRouter } from 'react-router-dom'
import { RootLayout } from '../layouts/RootLayout'
// import ProtectedRoute from './ProtectedRoute'
import { LoginPage } from '../features/auth/LoginPage'
import RegisterPage from '../features/auth/RegisterPage'
import HomePage from '../features/home/HomePage'
import RouterRoutes from '../utils/RouterRoutes'

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: RouterRoutes.LOGIN, element: <LoginPage /> },
      { path: RouterRoutes.REGISTER, element: <RegisterPage /> },
    ],
  },
])
