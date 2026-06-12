import React, { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Terminal, CheckCircle, XCircle, Clock, MemoryStick, ChevronDown, ChevronUp } from 'lucide-react'
import { useEditorStore } from '@/store/editorStore'
import { Badge, Skeleton } from '@/components/ui/index'

export const ExecutionPanel: React.FC = () => {
  const { executionResult, isRunning, submissionId } = useEditorStore()
  const [expanded, setExpanded] = useState(true)

  return (
    <div className="flex flex-col h-full" style={{ background: 'var(--bg-card)' }}>
      {/* Header */}
      <div
        className="flex items-center gap-2 px-3 py-2 border-b flex-shrink-0 cursor-pointer"
        style={{ borderColor: 'var(--border)', background: 'var(--bg-tertiary)' }}
        onClick={() => setExpanded(!expanded)}
      >
        <Terminal size={13} style={{ color: 'var(--accent)' }} />
        <span className="text-xs font-mono font-semibold flex-1" style={{ color: 'var(--text-primary)' }}>
          OUTPUT
        </span>

        {executionResult && (
          <Badge variant={executionResult.exitCode === 0 ? 'success' : 'error'} size="sm">
            {executionResult.exitCode === 0 ? 'OK' : 'ERR'}
          </Badge>
        )}

        {isRunning && (
          <span
            className="text-xs font-mono animate-pulse"
            style={{ color: 'var(--warning)' }}
          >
            ● running
          </span>
        )}

        {expanded ? (
          <ChevronUp size={12} style={{ color: 'var(--text-muted)' }} />
        ) : (
          <ChevronDown size={12} style={{ color: 'var(--text-muted)' }} />
        )}
      </div>

      {/* Body */}
      <AnimatePresence>
        {expanded && (
          <motion.div
            initial={{ height: 0 }}
            animate={{ height: 'auto' }}
            exit={{ height: 0 }}
            className="flex-1 overflow-hidden flex flex-col"
          >
            {isRunning ? (
              <div className="p-4 space-y-2">
                <Skeleton className="h-4 w-3/4" />
                <Skeleton className="h-4 w-1/2" />
                <Skeleton className="h-4 w-2/3" />
              </div>
            ) : executionResult ? (
              <div className="flex flex-col flex-1 overflow-hidden">
                {/* Stats bar */}
                <div
                  className="flex items-center gap-4 px-3 py-2 border-b flex-shrink-0"
                  style={{ borderColor: 'var(--border)', background: 'var(--bg-secondary)' }}
                >
                  <span className="flex items-center gap-1 text-xs font-mono" style={{ color: 'var(--text-muted)' }}>
                    {executionResult.exitCode === 0 ? (
                      <CheckCircle size={11} style={{ color: 'var(--success)' }} />
                    ) : (
                      <XCircle size={11} style={{ color: 'var(--error)' }} />
                    )}
                    exit {executionResult.exitCode}
                  </span>
                  <span className="flex items-center gap-1 text-xs font-mono" style={{ color: 'var(--text-muted)' }}>
                    <Clock size={11} />
                    {executionResult.executionTime}ms
                  </span>
                  {executionResult.memoryUsed && (
                    <span className="flex items-center gap-1 text-xs font-mono" style={{ color: 'var(--text-muted)' }}>
                      <MemoryStick size={11} />
                      {(executionResult.memoryUsed / 1024).toFixed(1)}KB
                    </span>
                  )}
                </div>

                {/* Output */}
                <div className="flex-1 overflow-auto p-3">
                  {executionResult.stdout && (
                    <div className="mb-3">
                      <p className="text-xs font-mono mb-1" style={{ color: 'var(--text-muted)' }}>
                        STDOUT
                      </p>
                      <pre
                        className="text-xs font-mono leading-5 whitespace-pre-wrap break-words p-3 rounded-lg"
                        style={{
                          background: 'var(--bg-secondary)',
                          color: 'var(--success)',
                        }}
                      >
                        {executionResult.stdout}
                      </pre>
                    </div>
                  )}

                  {executionResult.stderr && (
                    <div>
                      <p className="text-xs font-mono mb-1" style={{ color: 'var(--error)' }}>
                        STDERR
                      </p>
                      <pre
                        className="text-xs font-mono leading-5 whitespace-pre-wrap break-words p-3 rounded-lg"
                        style={{
                          background: 'rgba(245,101,101,0.08)',
                          color: 'var(--error)',
                        }}
                      >
                        {executionResult.stderr}
                      </pre>
                    </div>
                  )}

                  {!executionResult.stdout && !executionResult.stderr && (
                    <p className="text-xs font-mono" style={{ color: 'var(--text-muted)' }}>
                      No output produced.
                    </p>
                  )}
                </div>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center flex-1 p-6">
                <Terminal size={28} style={{ color: 'var(--text-muted)' }} className="mb-2" />
                <p className="text-xs font-mono text-center" style={{ color: 'var(--text-muted)' }}>
                  Run your code to see output
                </p>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
