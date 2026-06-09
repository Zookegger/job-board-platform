import * as React from "react"

import { cn } from "@/lib/utils"

interface InputProps extends React.ComponentProps<"input"> {
  startIcon?: React.ReactNode;
  endIcon?: React.ReactNode;
}

function Input({
  className,
  type,
  startIcon,
  endIcon,
  ...props
}: InputProps) {
  return (
    <div
      data-slot="input-wrapper"
      className={cn(
        "flex h-fit w-full min-w-0 items-center rounded-md border border-input bg-transparent px-2.5 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none",
        "focus-within:border-ring focus-within:ring-3 focus-within:ring-ring/50",
        "has-disabled:pointer-events-none has-disabled:cursor-not-allowed has-disabled:opacity-50",
        "aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20",
        "dark:bg-input/30 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40",
        className,
      )}
    >
      {startIcon && (
        <div className="mr-2 flex shrink-0 items-center justify-center text-muted-foreground">
          {startIcon}
        </div>
      )}
      <input
        type={type}
        data-slot="input"
        className="w-full flex-1 self-stretch bg-transparent py-0 text-base outline-none placeholder:text-muted-foreground disabled:cursor-not-allowed md:text-sm"
        {...props}
      />
      {endIcon && (
        <div className="ml-2 flex shrink-0 items-center justify-center text-muted-foreground">
          {endIcon}
        </div>
      )}
    </div>
  )
}

export { Input }
