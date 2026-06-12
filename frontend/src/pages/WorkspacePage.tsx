import React, { useState, useCallback, useRef, useEffect } from 'react'
import { motion } from 'framer-motion'
import { EditorPanel } from '@/modules/editor/EditorPanel'
import { ExecutionPanel } from '@/modules/execution/ExecutionPanel'
import { ComplexityPanel } from '@/modules/analytics/ComplexityPanel'
import { AIReviewPanel } from '@/modules/ai-review/AIReviewPanel'
import { Code2, BarChart2, Brain, Terminal, GripVertical } from 'lucide-react'

type PanelKey = 'editor' | 'output' | 'complexity' | 'ai'

interface PanelConfig {
  key: PanelKey
  label: string
  icon: React.ReactNode
  component: React.ReactNode
}

const PANELS: PanelConfig[] = [
  { key: 'editor',     label: 'Editor',     icon: <Code2 size={13} />,    component: <EditorPanel /> },
  { key: 'output',     label: 'Output',     icon: <Terminal size={13} />, component: <ExecutionPanel /> },
  { key: 'complexity', label: 'Complexity', icon: <BarChart2 size={13} />, component: <ComplexityPanel /> },
  { key: 'ai',         label: 'AI Review',  icon: <Brain size={13} />,    component: <AIReviewPanel /> },
]

export const WorkspacePage: React.FC = () => {
  // Left panel width in %
  const [leftWidth, setLeftWidth] = useState(50)
  // Right split: top panel height in %
  const [rightTopHeight, setRightTopHeight] = useState(50)
  // Active right-top tab and right-bottom tab
  const [rightTopTab, setRightTopTab] = useState<PanelKey>('complexity')
  const [rightBottomTab, setRightBottomTab] = useState<PanelKey>('ai')
  // Left panel bottom area height
  const [leftBottomHeight, setLeftBottomHeight] = useState(30)

  const containerRef = useRef<HTMLDivElement>(null)
  const isDraggingH = useRef(false)
  const isDraggingVTop = useRef(false)

  const handleHorizontalMouseDown = (e: React.MouseEvent) => {
    e.preventDefault()
    isDraggingH.current = true
  }

  const handleVerticalTopMouseDown = (e: React.MouseEvent) => {
    e.preventDefault()
    isDraggingVTop.current = true
  }

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!containerRef.current) return

      if (isDraggingH.current) {
        const rect = containerRef.current.getBoundingClientRect()
        const newWidth = ((e.clientX - rect.left) / rect.width) * 100
        setLeftWidth(Math.min(Math.max(newWidth, 25), 75))
      }

      if (isDraggingVTop.current) {
        const rect = containerRef.current.getBoundingClientRect()
        const relativeY = e.clientY - rect.top
        const newHeight = (relativeY / rect.height) * 100
        setRightTopHeight(Math.min(Math.max(newHeight, 20), 80))
      }
    }

    const handleMouseUp = () => {
      isDraggingH.current = false
      isDraggingVTop.current = false
    }

    window.addEventListener('mousemove', handleMouseMove)
    window.addEventListener('mouseup', handleMouseUp)
    return () => {
      window.removeEventListener('mousemove', handleMouseMove)
      window.removeEventListener('mouseup', handleMouseUp)
    }
  }, [])

  const TabBar: React.FC<{
    tabs: PanelKey[]
    active: PanelKey
    onChange: (k: PanelKey) => void
  }> = ({ tabs, active, onChange }) => (
    <div
      className="flex items-center border-b flex-shrink-0 px-1"
      style={{ borderColor: 'var(--border)', background: 'var(--bg-tertiary)', height: 34 }}
    >
      {tabs.map((key) => {
        const panel = PANELS.find((p) => p.key === key)!
        return (
          <button
            key={key}
            onClick={() => onChange(key)}
            className="flex items-center gap-1.5 px-3 h-full text-xs border-b-2 transition-all"
            style={{
              borderColor: active === key ? 'var(--accent)' : 'transparent',
              color: active === key ? 'var(--accent)' : 'var(--text-muted)',
              background: 'transparent',
              fontFamily: 'var(--font-mono)',
            }}
          >
            {panel.icon}
            {panel.label}
          </button>
        )
      })}
    </div>
  )

  const activeRightTop = PANELS.find((p) => p.key === rightTopTab)!
  const activeRightBottom = PANELS.find((p) => p.key === rightBottomTab)!

  return (
    <div
      ref={containerRef}
      className="flex h-full select-none overflow-hidden"
      style={{ background: 'var(--bg-primary)' }}
    >
      {/* ─── Left column: Editor + Output ─── */}
      <div
        className="flex flex-col overflow-hidden"
        style={{ width: `${leftWidth}%`, minWidth: 280 }}
      >
        {/* Editor fills top */}
        <div style={{ flex: `1 1 ${100 - leftBottomHeight}%`, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          <TabBar tabs={['editor']} active="editor" onChange={() => {}} />
          <div className="flex-1 overflow-hidden">
            <EditorPanel />
          </div>
        </div>

        {/* Output bottom */}
        <div
          className="flex-shrink-0 border-t overflow-hidden flex flex-col"
          style={{
            height: `${leftBottomHeight}%`,
            borderColor: 'var(--border)',
            minHeight: 80,
          }}
        >
          <ExecutionPanel />
        </div>
      </div>

      {/* ─── Horizontal Resize ─── */}
      <div
        className="flex-shrink-0 flex items-center justify-center cursor-col-resize hover:bg-[var(--accent)] transition-colors"
        style={{ width: 4, background: 'var(--border)' }}
        onMouseDown={handleHorizontalMouseDown}
      >
        <GripVertical size={10} style={{ color: 'var(--text-muted)', opacity: 0.5 }} />
      </div>

      {/* ─── Right column: Complexity + AI ─── */}
      <div className="flex flex-col flex-1 overflow-hidden" style={{ minWidth: 280 }}>
        {/* Top panel */}
        <div
          className="overflow-hidden flex flex-col"
          style={{ height: `${rightTopHeight}%`, minHeight: 100 }}
        >
          <TabBar
            tabs={['complexity', 'output']}
            active={rightTopTab === 'output' ? 'output' : 'complexity'}
            onChange={(k) => setRightTopTab(k)}
          />
          <div className="flex-1 overflow-hidden">
            {rightTopTab === 'complexity' ? <ComplexityPanel /> : <ExecutionPanel />}
          </div>
        </div>

        {/* Vertical Resize */}
        <div
          className="flex-shrink-0 cursor-row-resize hover:bg-[var(--accent)] transition-colors flex items-center justify-center"
          style={{ height: 4, background: 'var(--border)' }}
          onMouseDown={handleVerticalTopMouseDown}
        />

        {/* Bottom panel */}
        <div className="flex-1 overflow-hidden flex flex-col" style={{ minHeight: 100 }}>
          <TabBar
            tabs={['ai', 'complexity']}
            active={rightBottomTab === 'complexity' ? 'complexity' : 'ai'}
            onChange={(k) => setRightBottomTab(k)}
          />
          <div className="flex-1 overflow-hidden">
            {rightBottomTab === 'ai' ? <AIReviewPanel /> : <ComplexityPanel />}
          </div>
        </div>
      </div>
    </div>
  )
}
