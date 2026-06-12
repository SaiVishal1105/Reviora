import React, { useState } from 'react'
import { motion } from 'framer-motion'
import { Brain, Sparkles, Target, MessageSquare, ChevronRight, Loader2 } from 'lucide-react'
import { useEditorStore } from '@/store/editorStore'
import { aiService, getErrorMessage } from '@/services/api'
import { Button } from '@/components/ui/Button'
import { Badge, Skeleton } from '@/components/ui/index'
import toast from 'react-hot-toast'

export const AIReviewPanel: React.FC = () => {
  const { aiReview, setAIReview, submissionId, testCases, setTestCases } = useEditorStore()
  const [loadingReview, setLoadingReview] = useState(false)
  const [loadingTests, setLoadingTests] = useState(false)
  const [activeTab, setActiveTab] = useState<'review' | 'tests'>('review')

  const handleGetReview = async () => {
    if (!submissionId) return toast.error('Run your code first to get AI review')
    setLoadingReview(true)
    try {
      const review = await aiService.review(submissionId)
      setAIReview(review)
      toast.success('AI review ready')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoadingReview(false)
    }
  }

  const handleGenerateTests = async () => {
    if (!submissionId) return toast.error('Run your code first')
    setLoadingTests(true)
    try {
      const tests = await aiService.generateTests(submissionId)
      setTestCases(tests)
      toast.success(`Generated ${tests.length} test cases`)
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoadingTests(false)
    }
  }

  const CATEGORY_COLORS: Record<string, string> = {
    edge: 'var(--warning)',
    normal: 'var(--success)',
    stress: 'var(--error)',
    adversarial: 'var(--info)',
  }

  return (
    <div className="flex flex-col h-full" style={{ background: 'var(--bg-card)' }}>
      {/* Header */}
      <div
        className="flex items-center gap-2 px-3 py-2 border-b flex-shrink-0"
        style={{ borderColor: 'var(--border)', background: 'var(--bg-tertiary)' }}
      >
        <Brain size={13} style={{ color: 'var(--accent)' }} />
        <span className="text-xs font-mono font-semibold" style={{ color: 'var(--text-primary)' }}>
          AI REVIEW
        </span>
      </div>

      {/* Tabs */}
      <div
        className="flex border-b flex-shrink-0"
        style={{ borderColor: 'var(--border)' }}
      >
        {(['review', 'tests'] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className="px-4 py-2 text-xs font-mono capitalize border-b-2 transition-all"
            style={{
              borderColor: activeTab === tab ? 'var(--accent)' : 'transparent',
              color: activeTab === tab ? 'var(--accent)' : 'var(--text-muted)',
              background: 'transparent',
            }}
          >
            {tab === 'review' ? 'Optimization' : 'Test Cases'}
          </button>
        ))}
      </div>

      {/* Body */}
      <div className="flex-1 overflow-auto p-4">
        {activeTab === 'review' ? (
          <div className="space-y-3">
            {!aiReview && (
              <Button
                variant="secondary"
                size="sm"
                className="w-full"
                onClick={handleGetReview}
                loading={loadingReview}
                icon={<Sparkles size={12} />}
              >
                {loadingReview ? 'Analyzing...' : 'Get AI Review'}
              </Button>
            )}

            {loadingReview ? (
              <div className="space-y-3">
                <Skeleton className="h-16" />
                <Skeleton className="h-16" />
                <Skeleton className="h-10" />
              </div>
            ) : aiReview ? (
              <motion.div
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                className="space-y-3"
              >
                {/* Score */}
                <div
                  className="rounded-xl p-3 border flex items-center gap-3"
                  style={{ background: 'var(--bg-secondary)', borderColor: 'var(--border)' }}
                >
                  <div
                    className="w-12 h-12 rounded-full flex items-center justify-center text-base font-mono font-bold flex-shrink-0"
                    style={{
                      background: aiReview.codeQualityScore >= 8
                        ? 'rgba(62,207,142,0.15)'
                        : aiReview.codeQualityScore >= 5
                        ? 'rgba(246,173,85,0.15)'
                        : 'rgba(245,101,101,0.15)',
                      color: aiReview.codeQualityScore >= 8
                        ? 'var(--success)'
                        : aiReview.codeQualityScore >= 5
                        ? 'var(--warning)'
                        : 'var(--error)',
                    }}
                  >
                    {aiReview.codeQualityScore}/10
                  </div>
                  <div>
                    <p className="text-xs font-semibold" style={{ color: 'var(--text-primary)' }}>
                      Code Quality Score
                    </p>
                    <p className="text-xs mt-0.5" style={{ color: 'var(--text-secondary)' }}>
                      {aiReview.currentApproach}
                    </p>
                  </div>
                </div>

                {/* Optimization */}
                <div
                  className="rounded-xl p-3 border-l-2"
                  style={{
                    background: 'rgba(62,207,142,0.06)',
                    borderColor: 'var(--success)',
                  }}
                >
                  <div className="flex items-center gap-1.5 mb-1.5">
                    <Target size={11} style={{ color: 'var(--success)' }} />
                    <p className="text-xs font-mono font-semibold" style={{ color: 'var(--success)' }}>
                      SUGGESTED OPTIMIZATION
                    </p>
                  </div>
                  <p className="text-xs leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
                    {aiReview.suggestedOptimization}
                  </p>
                  {aiReview.expectedComplexity && (
                    <div className="flex items-center gap-2 mt-2">
                      <span className="text-xs font-mono" style={{ color: 'var(--text-muted)' }}>
                        Expected:
                      </span>
                      <Badge variant="success" size="sm">{aiReview.expectedComplexity}</Badge>
                    </div>
                  )}
                </div>

                {/* Interview notes */}
                {aiReview.interviewNotes && (
                  <div
                    className="rounded-xl p-3 border-l-2"
                    style={{
                      background: 'var(--accent-muted)',
                      borderColor: 'var(--accent)',
                    }}
                  >
                    <div className="flex items-center gap-1.5 mb-1.5">
                      <MessageSquare size={11} style={{ color: 'var(--accent)' }} />
                      <p className="text-xs font-mono font-semibold" style={{ color: 'var(--accent)' }}>
                        INTERVIEW NOTES
                      </p>
                    </div>
                    <p className="text-xs leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
                      {aiReview.interviewNotes}
                    </p>
                  </div>
                )}

                {/* Alternative approaches */}
                {aiReview.alternativeApproaches?.length > 0 && (
                  <div>
                    <p className="text-xs font-mono mb-2" style={{ color: 'var(--text-muted)' }}>
                      ALTERNATIVES
                    </p>
                    <div className="space-y-1.5">
                      {aiReview.alternativeApproaches.map((approach, i) => (
                        <div
                          key={i}
                          className="flex items-start gap-2 p-2 rounded-lg"
                          style={{ background: 'var(--bg-secondary)' }}
                        >
                          <ChevronRight size={11} className="mt-0.5 flex-shrink-0" style={{ color: 'var(--accent)' }} />
                          <p className="text-xs leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
                            {approach}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleGetReview}
                  loading={loadingReview}
                  className="w-full"
                >
                  Re-analyze
                </Button>
              </motion.div>
            ) : (
              <div className="flex flex-col items-center justify-center h-32">
                <Brain size={28} style={{ color: 'var(--text-muted)' }} className="mb-2" />
                <p className="text-xs font-mono text-center" style={{ color: 'var(--text-muted)' }}>
                  Run code first, then get AI review
                </p>
              </div>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            <Button
              variant="secondary"
              size="sm"
              className="w-full"
              onClick={handleGenerateTests}
              loading={loadingTests}
              icon={<Sparkles size={12} />}
            >
              {loadingTests ? 'Generating...' : testCases.length > 0 ? 'Regenerate Tests' : 'Generate Test Cases'}
            </Button>

            {loadingTests ? (
              <div className="space-y-2">
                {[1, 2, 3, 4].map((i) => (
                  <Skeleton key={i} className="h-14" />
                ))}
              </div>
            ) : testCases.length > 0 ? (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="space-y-2"
              >
                {testCases.map((tc, i) => (
                  <div
                    key={i}
                    className="rounded-xl border p-3"
                    style={{ background: 'var(--bg-secondary)', borderColor: 'var(--border)' }}
                  >
                    <div className="flex items-center gap-2 mb-2">
                      <span
                        className="text-xs px-1.5 py-0.5 rounded font-mono"
                        style={{
                          background: CATEGORY_COLORS[tc.category] + '20',
                          color: CATEGORY_COLORS[tc.category],
                        }}
                      >
                        {tc.category}
                      </span>
                      <span className="text-xs" style={{ color: 'var(--text-secondary)' }}>
                        {tc.description}
                      </span>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <p className="text-xs font-mono" style={{ color: 'var(--text-muted)' }}>IN</p>
                        <pre className="text-xs font-mono truncate" style={{ color: 'var(--text-primary)' }}>
                          {tc.input || '∅'}
                        </pre>
                      </div>
                      <div>
                        <p className="text-xs font-mono" style={{ color: 'var(--text-muted)' }}>OUT</p>
                        <pre className="text-xs font-mono truncate" style={{ color: 'var(--success)' }}>
                          {tc.expectedOutput || '∅'}
                        </pre>
                      </div>
                    </div>
                  </div>
                ))}
              </motion.div>
            ) : (
              <div className="flex flex-col items-center justify-center h-32">
                <Sparkles size={28} style={{ color: 'var(--text-muted)' }} className="mb-2" />
                <p className="text-xs font-mono text-center" style={{ color: 'var(--text-muted)' }}>
                  Generate edge cases and stress tests
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
