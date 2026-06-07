export function Footer() {
  return (
    <footer className="border-t bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-6">
        <p className="text-center text-sm text-gray-500">
          &copy; {new Date().getFullYear()} JobBoard. All rights reserved.
        </p>
      </div>
    </footer>
  )
}
