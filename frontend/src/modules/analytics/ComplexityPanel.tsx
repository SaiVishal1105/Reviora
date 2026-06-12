import React from 'react'
import { motion } from 'framer-motion'
import {
  LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts'
import { GitBranch, AlertTriangle, Activity, Layers } from 'lucide-react'
import { useEditorStore } from '@/store/editorStore'
import { Badge, Skeleton } from '@/components/ui/index'

const COMPLEXITY_CONFIG: Record<string, { color: string; label: string; severity: 'success' | 'info' | 'warning' | 'error' }> = {
  'O(1)':       { color: 'var(--success)', label: 'Constant',     severity: 'success' },
  'O(log n)':   { color: 'var(--info)',    label: 'Logarithmic',  severity: 'info'    },
  'O(n)':       { color: 'var(--accent)',  label: 'Linear',       severity: 'info'    },
  'O(n log n)': { color: 'var(--warning)', label: 'Log-Linear',   severity: 'warning' },
  'O(n²)':      { color: 'var(--warning)', label: 'Quadratic',    severity: 'warning' },
  'O(n³)':      { color: 'var(--error)',   label: 'Cubic',        severity: 'error'   },
  'O(2^n)':     { color: 'var(--error)',   label: 'Exponential',  severity: 'error'   },
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div
        className="rounded-lg border p-2 text-xs font-mono shadow-xl"
        style={{ background: 'var(--bg-card)', borderColor: 'var(--border)' }}
      >
        <p style={{ color: 'var(--text-muted)' }}>n = {label.toLocaleString()}</p>
        <p style={{ color: 'var(--accent)' }}>
          {payload[0]?.value?.toLocaleString()} ops
        </p>
        <p style={{ color: 'var(--text-secondary)' }}>
          {payload[1]?.value?.toFixed(2)} ms
        </p>
      </div>
    )
  }
  return null
}

export const ComplexityPanel: React.FC = () => {
  const { complexityReport, growthData, isAnalyzing } = useEditorStore()

  const config = complexityReport
    ? COMPLEXITY_CONFIG[complexityReport.timeComplexity] || {
        color: 'var(--text-secondary)',
        label: 'Unknown',
        severity: 'default' as any,
      }
    : null

  return (
    <div className="flex flex-col h-full overflow-auto" style={{ background: 'var(--bg-card)' }}>
      {/* Header */}
      <div
        className="flex items-center gap-2 px-3 py-2 border-b flex-shrink-0"
        style={{ borderColor: 'var(--border)', background: 'var(--bg-tertiary)' }}
      >
        <Activity size={13} style={{ color: 'var(--accent)' }} />
        <span className="text-xs font-mono font-semibold" style={{ color: 'var(--text-primary)' }}>
          COMPLEXITY ANALYSIS
        </span>
        {isAnalyzing && (
          <span className="text-xs font-mono animate-pulse ml-auto" style={{ color: 'var(--warning)' }}>
            ● analyzing
          </span>
        )}
      </div>

      <div className="flex-1 p-4 space-y-4 overflow-auto">
        {isAnalyzing ? (
          <div className="space-y-3">
            <Skeleton className="h-12 w-1/2" />
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-32" />
          </div>
        ) : complexityReport ? (
          <>
            {/* Complexity badges */}
            <div className="grid grid-cols-2 gap-3">
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                className="rounded-xl p-3 border"
                style={{
                  background: 'var(--bg-secondary)',
                  borderColor: config!.color + '40',
                }}
              >
                <p className="text-xs font-mono mb-1" style={{ color: 'var(--text-muted)' }}>
                  TIME
                </p>
                <p
                  className="text-xl font-mono font-bold"
                  style={{ color: config!.color }}
                >
                  {complexityReport.timeComplexity}
                </p>
                <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
                  {config!.label}
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.1 }}
                className="rounded-xl p-3 border"
                style={{ background: 'var(--bg-secondary)', borderColor: 'var(--border)' }}
              >
                <p className="text-xs font-mono mb-1" style={{ color: 'var(--text-muted)' }}>
                  SPACE
                </p>
                <p className="text-xl font-mono font-bold" style={{ color: 'var(--info)' }}>
                  {complexityReport.spaceComplexity}
                </p>
                <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
                  Memory usage
                </p>
              </motion.div>
            </div>

            {/* Pattern + stats */}
            <div
              className="rounded-xl p-3 border space-y-2"
              style={{ background: 'var(--bg-secondary)', borderColor: 'var(--border)' }}
            >
              <div className="flex items-center gap-2 justify-between">
                <span className="text-xs font-mono" style={{ color: 'var(--text-muted)' }}>
                  PATTERN
                </span>
                <Badge variant="accent" size="sm">{complexityReport.pattern}</Badge>
              </div>
              <div className="grid grid-cols-3 gap-2 pt-1">
                {[
                  { label: 'Loops', value: complexityReport.loopCount },
                  { label: 'Depth', value: complexityReport.nestingDepth },
                  { label: 'Recursion', value: complexityReport.recursionDetected ? 'Yes' : 'No' },
                ].map((stat) => (
                  <div key={stat.label} className="text-center">
                    <p
                      className="text-base font-mono font-bold"
                      style={{ color: 'var(--text-primary)' }}
                    >
                      {stat.value}
                    </p>
                    <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
                      {stat.label}
                    </p>
                  </div>
                ))}
              </div>
            </div>

            {/* Explanation */}
            {complexityReport.explanation && (
              <div
                className="rounded-xl p-3 border-l-2 text-xs leading-relaxed"
                style={{
                  background: 'var(--accent-muted)',
                  borderColor: 'var(--accent)',
                  color: 'var(--text-secondary)',
                }}
              >
                {complexityReport.explanation}
              </div>
            )}

            {/* Growth chart */}
            {growthData && growthData.length > 0 && (
              <div>
                <p className="text-xs font-mono mb-2" style={{ color: 'var(--text-muted)' }}>
                  GROWTH CURVE
                </p>
                <div
                  className="rounded-xl p-3 border"
                  style={{ background: 'var(--bg-secondary)', borderColor: 'var(--border)' }}
                >
                  <ResponsiveContainer width="100%" height={140}>
                    <LineChart data={growthData}>
                      <CartesianGrid
                        strokeDasharray="3 3"
                        stroke="var(--border)"
                        opacity={0.5}
                      />
                      <XAxis
                        dataKey="inputSize"
                        tick={{ fontSize: 9, fill: 'var(--text-muted)', fontFamily: 'JetBrains Mono' }}
                        tickFormatter={(v) => v >= 1000 ? `${v / 1000}k` : v}
                      />
                      <YAxis
                        tick={{ fontSize: 9, fill: 'var(--text-muted)', fontFamily: 'JetBrains Mono' }}
                        tickFormatter={(v) => v >= 1_000_000 ? `${(v / 1_000_000).toFixed(1)}M` : v >= 1000 ? `${(v / 1000).toFixed(0)}k` : v}
                        width={40}
                      />
                      <Tooltip content={<CustomTooltip />} />
                      <Line
                        type="monotone"
                        dataKey="operations"
                        stroke="var(--accent)"
                        strokeWidth={2}
                        dot={false}
                        activeDot={{ r: 4, fill: 'var(--accent)' }}
                      />
                      <Line
                        type="monotone"
                        dataKey="executionTimeMs"
                        stroke="var(--success)"
                        strokeWidth={1.5}
                        dot={false}
                        strokeDasharray="4 2"
                        activeDot={{ r: 3, fill: 'var(--success)' }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                  <div className="flex gap-4 mt-2 justify-center">
                    <span className="text-xs font-mono flex items-center gap-1" style={{ color: 'var(--accent)' }}>
                      <span className="inline-block w-4 h-0.5" style={{ background: 'var(--accent)' }} />
                      Operations
                    </span>
                    <span className="text-xs font-mono flex items-center gap-1" style={{ color: 'var(--success)' }}>
                      <span className="inline-block w-4 h-0.5 border-t-2 border-dashed" style={{ borderColor: 'var(--success)' }} />
                      Time (ms)
                    </span>
                  </div>
                </div>
              </div>
            )}
          </>
        ) : (
          <div className="flex flex-col items-center justify-center h-40">
            <GitBranch size={28} style={{ color: 'var(--text-muted)' }} className="mb-2" />
            <p className="text-xs font-mono text-center" style={{ color: 'var(--text-muted)' }}>
              Run code to analyze complexity
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
