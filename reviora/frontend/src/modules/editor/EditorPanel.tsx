import React, { useCallback } from 'react'
import Editor from '@monaco-editor/react'
import { useEditorStore, type Language } from '@/store/editorStore'
import { useThemeStore } from '@/store/themeStore'
import { useAuthStore } from '@/store/authStore'
import { Select } from '@/components/ui/index'
import { Button } from '@/components/ui/Button'
import { Play, RotateCcw, ChevronDown } from 'lucide-react'
import { submissionService, getErrorMessage } from '@/services/api'
import toast from 'react-hot-toast'

const LANGUAGE_OPTIONS = [
  { value: 'cpp',        label: 'C++'        },
  { value: 'java',       label: 'Java'       },
  { value: 'python',     label: 'Python'     },
  { value: 'javascript', label: 'JavaScript' },
  { value: 'typescript', label: 'TypeScript' },
  { value: 'go',         label: 'Go'         },
  { value: 'rust',       label: 'Rust'       },
  { value: 'c',          label: 'C'          },
]

const PISTON_LANG_MAP: Record<Language, string> = {
  cpp:        'cpp',
  java:       'java',
  python:     'python3',
  javascript: 'javascript',
  typescript: 'typescript',
  go:         'go',
  rust:       'rust',
  c:          'c',
}

const MONACO_LANG_MAP: Record<Language, string> = {
  cpp:        'cpp',
  java:       'java',
  python:     'python',
  javascript: 'javascript',
  typescript: 'typescript',
  go:         'go',
  rust:       'rust',
  c:          'c',
}

const THEME_MAP: Record<string, string> = {
  graphite: 'reviora-dark',
  aurora:   'reviora-aurora',
  ivory:    'reviora-light',
  matrix:   'reviora-matrix',
}

export const EditorPanel: React.FC = () => {
  // All hooks inside the component
  const { isAuthenticated } = useAuthStore()
  const { theme } = useThemeStore()
  const {
    code, setCode, language, setLanguage, stdin,
    isRunning, setIsRunning, setExecutionResult,
    setComplexityReport, setSubmissionId, setGrowthData,
    setIsAnalyzing,
  } = useEditorStore()

  const defineThemes = useCallback((monaco: any) => {
    monaco.editor.defineTheme('reviora-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [
        { token: 'comment', foreground: '4a4a52', fontStyle: 'italic' },
        { token: 'keyword', foreground: '5b8def' },
        { token: 'string',  foreground: '3ecf8e' },
        { token: 'number',  foreground: 'f6ad55' },
        { token: 'type',    foreground: '63b3ed' },
      ],
      colors: {
        'editor.background':                 '#141416',
        'editor.foreground':                 '#f0f0f2',
        'editor.lineHighlightBackground':    '#1a1a1d',
        'editorLineNumber.foreground':       '#2a2a2f',
        'editorLineNumber.activeForeground': '#5b8def',
        'editor.selectionBackground':        '#5b8def30',
        'editor.inactiveSelectionBackground':'#5b8def15',
        'editorCursor.foreground':           '#5b8def',
        'editorGutter.background':           '#141416',
      },
    })

    monaco.editor.defineTheme('reviora-aurora', {
      base: 'vs-dark',
      inherit: true,
      rules: [
        { token: 'comment', foreground: '3a5a72', fontStyle: 'italic' },
        { token: 'keyword', foreground: '00d4ff' },
        { token: 'string',  foreground: '00e8a4' },
        { token: 'number',  foreground: 'ffb347' },
        { token: 'type',    foreground: '7cb9f0' },
      ],
      colors: {
        'editor.background':                 '#0d1422',
        'editor.foreground':                 '#e8f4fd',
        'editor.lineHighlightBackground':    '#111c2e',
        'editorLineNumber.foreground':       '#1e3352',
        'editorLineNumber.activeForeground': '#00d4ff',
        'editor.selectionBackground':        '#00d4ff25',
        'editorCursor.foreground':           '#00d4ff',
        'editorGutter.background':           '#0d1422',
      },
    })

    monaco.editor.defineTheme('reviora-light', {
      base: 'vs',
      inherit: true,
      rules: [
        { token: 'comment', foreground: 'b0aaa0', fontStyle: 'italic' },
        { token: 'keyword', foreground: '2563eb' },
        { token: 'string',  foreground: '16a34a' },
        { token: 'number',  foreground: 'd97706' },
      ],
      colors: {
        'editor.background':                 '#f5f5f0',
        'editor.foreground':                 '#1a1916',
        'editor.lineHighlightBackground':    '#eeede8',
        'editorLineNumber.foreground':       '#dcdbd4',
        'editorLineNumber.activeForeground': '#2563eb',
        'editorCursor.foreground':           '#2563eb',
        'editorGutter.background':           '#f5f5f0',
      },
    })

    monaco.editor.defineTheme('reviora-matrix', {
      base: 'vs-dark',
      inherit: true,
      rules: [
        { token: 'comment', foreground: '005515', fontStyle: 'italic' },
        { token: 'keyword', foreground: '00ff41', fontStyle: 'bold' },
        { token: 'string',  foreground: '00cc33' },
        { token: 'number',  foreground: '33ff66' },
        { token: 'type',    foreground: '00ccff' },
      ],
      colors: {
        'editor.background':                 '#020502',
        'editor.foreground':                 '#00ff41',
        'editor.lineHighlightBackground':    '#040804',
        'editorLineNumber.foreground':       '#0d2010',
        'editorLineNumber.activeForeground': '#00ff41',
        'editor.selectionBackground':        '#00ff4120',
        'editorCursor.foreground':           '#00ff41',
        'editorGutter.background':           '#020502',
      },
    })
  }, [])

  const handleRun = async () => {
    if (!code.trim()) return toast.error('Please write some code first')

    setIsRunning(true)
    setExecutionResult(null)

    try {
      // Guests hit the public endpoint; logged-in users hit the authenticated one
      const data = isAuthenticated
        ? await submissionService.submit({ code, language: PISTON_LANG_MAP[language], stdin })
        : await submissionService.submitGuest({ code, language: PISTON_LANG_MAP[language], stdin })

      setExecutionResult(data.executionResult)

      if (data.id) {
        setSubmissionId(data.id)
        setIsAnalyzing(true)
        try {
          const analysis = await submissionService.analyze(data.id)
          setComplexityReport(analysis)

          const growth = await submissionService.getGrowthData(data.id)
          setGrowthData(growth)
        } catch (err) {
          console.warn('Analysis failed', err)
        } finally {
          setIsAnalyzing(false)
        }
      }

      toast.success('Execution complete')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setIsRunning(false)
    }
  }

  return (
    <div className="flex flex-col h-full" style={{ background: 'var(--bg-secondary)' }}>
      {/* Toolbar */}
      <div
        className="flex items-center gap-2 px-3 py-2 border-b flex-shrink-0"
        style={{ borderColor: 'var(--border)', background: 'var(--bg-tertiary)' }}
      >
        <Select
          value={language}
          onChange={(v) => setLanguage(v as Language)}
          options={LANGUAGE_OPTIONS}
          className="text-xs"
        />
        <div className="flex-1" />
        <Button
          size="sm"
          variant="ghost"
          onClick={() => setCode('')}
          icon={<RotateCcw size={12} />}
        >
          Clear
        </Button>
        <Button
          size="sm"
          variant="primary"
          onClick={handleRun}
          loading={isRunning}
          icon={<Play size={12} />}
        >
          {isRunning ? 'Running...' : 'Run'}
        </Button>
      </div>

      {/* Monaco Editor */}
      <div className="flex-1 overflow-hidden">
        <Editor
          height="100%"
          language={MONACO_LANG_MAP[language]}
          value={code}
          onChange={(val) => setCode(val || '')}
          theme={THEME_MAP[theme] || 'reviora-dark'}
          beforeMount={defineThemes}
          options={{
            fontSize: 13,
            fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
            fontLigatures: true,
            minimap: { enabled: false },
            scrollBeyondLastLine: false,
            lineNumbers: 'on',
            glyphMargin: false,
            folding: true,
            padding: { top: 12, bottom: 12 },
            renderLineHighlight: 'gutter',
            cursorBlinking: 'smooth',
            smoothScrolling: true,
            tabSize: 4,
            wordWrap: 'on',
            automaticLayout: true,
            scrollbar: {
              verticalScrollbarSize: 4,
              horizontalScrollbarSize: 4,
            },
          }}
        />
      </div>

      {/* Stdin */}
      <div
        className="border-t flex-shrink-0"
        style={{ borderColor: 'var(--border)', background: 'var(--bg-tertiary)' }}
      >
        <div
          className="flex items-center gap-2 px-3 py-1.5 border-b"
          style={{ borderColor: 'var(--border)' }}
        >
          <ChevronDown size={11} style={{ color: 'var(--text-muted)' }} />
          <span className="text-xs font-mono" style={{ color: 'var(--text-muted)' }}>
            STDIN
          </span>
        </div>
        <textarea
          value={stdin}
          onChange={(e) => useEditorStore.getState().setStdin(e.target.value)}
          className="w-full px-3 py-2 text-xs font-mono resize-none focus:outline-none"
          rows={3}
          placeholder="Input for your program..."
          style={{
            background: 'transparent',
            color: 'var(--text-secondary)',
          }}
        />
      </div>
    </div>
  )
}