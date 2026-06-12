import React from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  Zap, BarChart2, Brain, Play, ArrowRight, GitBranch,
  Clock, Shield, Layers, ChevronRight,
} from 'lucide-react'

const FEATURES = [
  { icon: <GitBranch size={20} />, title: 'Static AST Analysis', desc: 'Parser-level complexity detection without AI — pure engineering.' },
  { icon: <BarChart2 size={20} />, title: 'Runtime Instrumentation', desc: 'Tracks actual loop iterations, comparisons, and recursion depth.' },
  { icon: <Layers size={20} />, title: 'Growth Visualization', desc: 'Visualize O(n²) vs O(n log n) growth across input scales.' },
  { icon: <Brain size={20} />, title: 'AI Optimization Review', desc: 'AI suggests improvements — after the deterministic engine runs.' },
  { icon: <Play size={20} />, title: 'Execution Replay', desc: 'Step through variable mutations and pointer movements visually.' },
  { icon: <Shield size={20} />, title: 'AI Test Generator', desc: 'Generates edge cases, adversarial inputs, and stress tests.' },
]

const COMPLEXITIES = [
  { label: 'O(1)',       color: 'var(--success)' },
  { label: 'O(log n)',   color: 'var(--info)'    },
  { label: 'O(n)',       color: 'var(--accent)'  },
  { label: 'O(n log n)', color: 'var(--warning)' },
  { label: 'O(n²)',      color: '#f6ad55'        },
  { label: 'O(2ⁿ)',      color: 'var(--error)'   },
]

export const HomePage: React.FC = () => {
  return (
    <div className="overflow-auto" style={{ height: 'calc(100vh - 48px)' }}>
      {/* Hero */}
      <section className="relative px-6 pt-20 pb-16 max-w-5xl mx-auto text-center">
        <div
          className="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-96 rounded-full blur-3xl opacity-10 pointer-events-none"
          style={{ background: 'var(--accent)' }}
        />

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
          <span
            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-mono border mb-6"
            style={{ background: 'var(--accent-muted)', borderColor: 'var(--accent)', color: 'var(--accent)' }}
          >
            <Zap size={10} /> Algorithm Engineering Platform
          </span>
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.1 }}
          className="text-5xl sm:text-6xl font-bold mb-5 leading-tight"
          style={{ fontFamily: 'var(--font-display)', color: 'var(--text-primary)' }}
        >
          Revi{''}
          <span style={{
            background: 'linear-gradient(135deg, var(--accent), var(--accent-hover))',
            WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text',
          }}>
            ora
          </span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.2 }}
          className="text-base sm:text-lg max-w-2xl mx-auto mb-8 leading-relaxed"
          style={{ color: 'var(--text-secondary)' }}
        >
          Reviora is an intelligent algorithm analysis operating system. Not just a compiler
          a platform that measures, visualizes, and explains algorithmic behavior at engineering depth.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.3 }}
          className="flex items-center justify-center gap-3 flex-wrap"
        >
          <Link to="/workspace" className="inline-flex items-center gap-2 px-6 py-3 rounded-xl text-sm font-semibold transition-all hover:opacity-90" style={{ background: 'var(--accent)', color: 'white' }}>
            Open Workspace <ArrowRight size={15} />
          </Link>
          <Link to="/register" className="inline-flex items-center gap-2 px-6 py-3 rounded-xl text-sm font-semibold border transition-all hover:bg-[var(--bg-hover)]" style={{ background: 'transparent', borderColor: 'var(--border)', color: 'var(--text-secondary)' }}>
            Create account
          </Link>
        </motion.div>

        <motion.div
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5, delay: 0.4 }}
          className="flex items-center justify-center gap-2 mt-10 flex-wrap"
        >
          {COMPLEXITIES.map((c) => (
            <span key={c.label} className="px-3 py-1 rounded-full text-xs font-mono border"
              style={{ color: c.color, borderColor: c.color + '40', background: c.color + '12' }}>
              {c.label}
            </span>
          ))}
        </motion.div>
      </section>

      {/* Terminal preview */}
      <section className="px-6 pb-16 max-w-4xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, delay: 0.4 }}
          className="rounded-2xl border overflow-hidden shadow-2xl"
          style={{ background: 'var(--bg-card)', borderColor: 'var(--border)' }}
        >
          <div className="flex items-center gap-2 px-4 py-3 border-b" style={{ background: 'var(--bg-secondary)', borderColor: 'var(--border)' }}>
            <div className="w-3 h-3 rounded-full bg-red-500 opacity-80" />
            <div className="w-3 h-3 rounded-full bg-yellow-500 opacity-80" />
            <div className="w-3 h-3 rounded-full bg-green-500 opacity-80" />
            <span className="ml-2 text-xs font-mono" style={{ color: 'var(--text-muted)' }}>reviora — workspace</span>
          </div>
          <div className="grid grid-cols-2" style={{ borderTop: '1px solid var(--border)' }}>
            <div className="p-5 border-r" style={{ borderColor: 'var(--border)' }}>
              <p className="text-xs font-mono mb-3" style={{ color: 'var(--text-muted)' }}>// Two Sum — brute force</p>
              <pre className="text-xs leading-6 font-mono" style={{ color: 'var(--text-primary)' }}>
{`for(int i=0;i<n;i++) {\n`}
<span style={{ color: 'var(--warning)' }}>{`  for(int j=i+1;j<n;j++)`}</span>{`\n`}
<span style={{ color: 'var(--error)' }}>{`    if(arr[i]+arr[j]==t)`}</span>{`\n`}
<span style={{ color: 'var(--success)' }}>{`      return {i,j};\n}`}</span>
              </pre>
            </div>
            <div className="p-5 space-y-3">
              <div>
                <p className="text-xs font-mono mb-1" style={{ color: 'var(--text-muted)' }}>DETECTED COMPLEXITY</p>
                <p className="text-2xl font-mono font-bold" style={{ color: '#f6ad55' }}>O(n²)</p>
              </div>
              <div>
                <p className="text-xs font-mono mb-1" style={{ color: 'var(--text-muted)' }}>PATTERN</p>
                <p className="text-sm font-mono" style={{ color: 'var(--text-secondary)' }}>Nested iteration detected</p>
              </div>
              <div className="p-3 rounded-lg border-l-2 text-xs" style={{ background: 'var(--accent-muted)', borderColor: 'var(--accent)', color: 'var(--text-secondary)' }}>
                <span style={{ color: 'var(--accent)' }}>AI Suggestion:</span> Use HashMap for O(n) lookup.
              </div>
            </div>
          </div>
        </motion.div>
      </section>

      {/* Features */}
      <section className="px-6 pb-20 max-w-5xl mx-auto">
        <motion.div initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }} className="text-center mb-12">
          <h2 className="text-3xl font-bold mb-3" style={{ fontFamily: 'var(--font-display)', color: 'var(--text-primary)' }}>
            Engineered. Not generated.
          </h2>
          <p style={{ color: 'var(--text-secondary)' }}>Every feature is technically grounded — not an AI wrapper.</p>
        </motion.div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {FEATURES.map((f, i) => (
            <motion.div
              key={f.title}
              initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}
              transition={{ delay: i * 0.07 }}
              className="p-5 rounded-xl border transition-all cursor-default hover:-translate-y-0.5"
              style={{ background: 'var(--bg-card)', borderColor: 'var(--border)' }}
            >
              <div className="w-9 h-9 rounded-lg flex items-center justify-center mb-3" style={{ background: 'var(--accent-muted)', color: 'var(--accent)' }}>
                {f.icon}
              </div>
              <h3 className="text-sm font-semibold mb-1" style={{ color: 'var(--text-primary)', fontFamily: 'var(--font-display)' }}>{f.title}</h3>
              <p className="text-xs leading-relaxed" style={{ color: 'var(--text-secondary)' }}>{f.desc}</p>
            </motion.div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className="px-6 pb-24 max-w-2xl mx-auto text-center">
        <motion.div
          initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}
          className="p-10 rounded-2xl border"
          style={{ background: 'var(--bg-card)', borderColor: 'var(--border)' }}
        >
          <Clock size={32} className="mx-auto mb-4" style={{ color: 'var(--accent)' }} />
          <h2 className="text-2xl font-bold mb-3" style={{ fontFamily: 'var(--font-display)', color: 'var(--text-primary)' }}>
            Start analyzing now
          </h2>
          <p className="text-sm mb-6" style={{ color: 'var(--text-secondary)' }}>Workspace is free. Full analytics and AI review on signup.</p>
          <Link to="/workspace" className="inline-flex items-center gap-2 px-6 py-3 rounded-xl text-sm font-semibold hover:opacity-90 transition-opacity" style={{ background: 'var(--accent)', color: 'white' }}>
            Open Workspace <ChevronRight size={15} />
          </Link>
        </motion.div>
      </section>
    </div>
  )
}
