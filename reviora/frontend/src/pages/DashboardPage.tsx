import React from 'react'
import { motion } from 'framer-motion'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
  Cell,
} from 'recharts'
import { Flame, Code2, TrendingUp, Target, Clock, Star, CheckCircle, AlertTriangle, ArrowRight } from 'lucide-react'
import { useAuthStore } from '@/store/authStore'
import { Badge } from '@/components/ui/index'
import { Link } from 'react-router-dom'

const MOCK = {
  totalSubmissions: 142, acceptedSubmissions: 118,
  currentStreak: 7, longestStreak: 23, averageComplexityScore: 7.4,
  topics: [
    { topic:'Arrays',  solved:28, total:35, score:80 },
    { topic:'Strings', solved:18, total:22, score:82 },
    { topic:'Trees',   solved:15, total:25, score:60 },
    { topic:'Graphs',  solved:8,  total:20, score:40 },
    { topic:'DP',      solved:6,  total:18, score:33 },
    { topic:'Greedy',  solved:12, total:15, score:80 },
    { topic:'Sorting', solved:20, total:22, score:91 },
    { topic:'BFS/DFS', solved:11, total:16, score:69 },
  ],
  recent: [
    { id:'1', problem:'Two Sum',      complexity:'O(n)',       status:'accepted', language:'cpp',    time:'2h ago'  },
    { id:'2', problem:'Binary Search',complexity:'O(log n)',   status:'accepted', language:'java',   time:'5h ago'  },
    { id:'3', problem:'Merge Sort',   complexity:'O(n log n)', status:'accepted', language:'python', time:'1d ago'  },
    { id:'4', problem:'N-Queens',     complexity:'O(n!)',      status:'failed',   language:'cpp',    time:'2d ago'  },
    { id:'5', problem:'LRU Cache',    complexity:'O(1)',       status:'accepted', language:'java',   time:'3d ago'  },
  ],
  complexityDist: [
    { name:'O(1)',      count:12, color:'#3ecf8e' },
    { name:'O(log n)',  count:18, color:'#63b3ed' },
    { name:'O(n)',      count:34, color:'#5b8def' },
    { name:'O(n log n)',count:28, color:'#f6ad55' },
    { name:'O(n²)',     count:19, color:'#ed8936' },
    { name:'O(2ⁿ)',     count: 7, color:'#f56565' },
  ],
}

const RADAR_DATA = MOCK.topics.map(t => ({ subject: t.topic, A: t.score }))

const StatCard: React.FC<{ label:string; value:string|number; sub?:string; icon:React.ReactNode; color:string; delay?:number }> =
  ({ label, value, sub, icon, color, delay=0 }) => (
  <motion.div
    initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }} transition={{ duration:0.4, delay }}
    className="rounded-xl border p-4 flex flex-col gap-2"
    style={{ background:'var(--bg-card)', borderColor:'var(--border)' }}
  >
    <div className="flex items-center justify-between">
      <p className="text-xs font-mono" style={{ color:'var(--text-muted)' }}>{label}</p>
      <div className="w-7 h-7 rounded-lg flex items-center justify-center" style={{ background: color+'20', color }}>
        {icon}
      </div>
    </div>
    <p className="text-2xl font-bold" style={{ color:'var(--text-primary)', fontFamily:'var(--font-display)' }}>{value}</p>
    {sub && <p className="text-xs" style={{ color:'var(--text-muted)' }}>{sub}</p>}
  </motion.div>
)

export const DashboardPage: React.FC = () => {
  const { user } = useAuthStore()
  const acceptanceRate = Math.round((MOCK.acceptedSubmissions / MOCK.totalSubmissions) * 100)

  return (
    <div className="h-full overflow-auto p-5 space-y-5" style={{ background:'var(--bg-primary)' }}>
      {/* Header */}
      <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }} className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold" style={{ fontFamily:'var(--font-display)', color:'var(--text-primary)' }}>
            Welcome back, {user?.name?.split(' ')[0] || 'Developer'}
          </h1>
          <p className="text-sm mt-0.5" style={{ color:'var(--text-secondary)' }}>Your algorithm engineering overview</p>
        </div>
        <Link to="/workspace" className="flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold hover:opacity-90 transition-opacity" style={{ background:'var(--accent)', color:'white' }}>
          Open Workspace <ArrowRight size={12} />
        </Link>
      </motion.div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <StatCard label="TOTAL SUBMISSIONS" value={MOCK.totalSubmissions}         sub={`${MOCK.acceptedSubmissions} accepted`}       icon={<Code2 size={14} />}      color="var(--accent)"   delay={0.05} />
        <StatCard label="ACCEPTANCE RATE"   value={`${acceptanceRate}%`}          sub="All time"                                     icon={<CheckCircle size={14} />} color="var(--success)"  delay={0.10} />
        <StatCard label="CURRENT STREAK"    value={`${MOCK.currentStreak}d`}      sub={`Best: ${MOCK.longestStreak}d`}                icon={<Flame size={14} />}       color="var(--warning)"  delay={0.15} />
        <StatCard label="AVG QUALITY SCORE" value={`${MOCK.averageComplexityScore}/10`} sub="Code quality"                          icon={<Star size={14} />}        color="var(--info)"     delay={0.20} />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }} transition={{ delay:0.25 }}
          className="rounded-xl border p-4" style={{ background:'var(--bg-card)', borderColor:'var(--border)' }}>
          <p className="text-xs font-mono mb-4" style={{ color:'var(--text-muted)' }}>COMPLEXITY DISTRIBUTION</p>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={MOCK.complexityDist} barSize={26}>
              <XAxis dataKey="name" tick={{ fontSize:9, fill:'var(--text-muted)', fontFamily:'JetBrains Mono' }} />
              <YAxis tick={{ fontSize:9, fill:'var(--text-muted)' }} width={24} />
              <Tooltip contentStyle={{ background:'var(--bg-secondary)', border:'1px solid var(--border)', borderRadius:8, fontSize:11, fontFamily:'JetBrains Mono', color:'var(--text-primary)' }} />
              <Bar dataKey="count" radius={[4,4,0,0]}>
                {MOCK.complexityDist.map((entry, i) => <Cell key={i} fill={entry.color} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </motion.div>

        <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }} transition={{ delay:0.3 }}
          className="rounded-xl border p-4" style={{ background:'var(--bg-card)', borderColor:'var(--border)' }}>
          <p className="text-xs font-mono mb-4" style={{ color:'var(--text-muted)' }}>TOPIC PROFICIENCY</p>
          <ResponsiveContainer width="100%" height={180}>
            <RadarChart data={RADAR_DATA}>
              <PolarGrid stroke="var(--border)" />
              <PolarAngleAxis dataKey="subject" tick={{ fontSize:9, fill:'var(--text-muted)', fontFamily:'JetBrains Mono' }} />
              <PolarRadiusAxis angle={90} domain={[0,100]} tick={{ fontSize:8, fill:'var(--text-muted)' }} />
              <Radar name="Score" dataKey="A" stroke="var(--accent)" fill="var(--accent)" fillOpacity={0.15} strokeWidth={1.5} />
            </RadarChart>
          </ResponsiveContainer>
        </motion.div>
      </div>

      {/* Topic breakdown */}
      <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }} transition={{ delay:0.35 }}
        className="rounded-xl border" style={{ background:'var(--bg-card)', borderColor:'var(--border)' }}>
        <div className="px-4 py-3 border-b flex items-center gap-2" style={{ borderColor:'var(--border)' }}>
          <Target size={13} style={{ color:'var(--accent)' }} />
          <p className="text-xs font-mono font-semibold" style={{ color:'var(--text-primary)' }}>TOPIC BREAKDOWN</p>
        </div>
        <div className="p-4 space-y-3">
          {MOCK.topics.map((topic) => (
            <div key={topic.topic} className="flex items-center gap-3">
              <span className="text-xs font-mono w-20 flex-shrink-0" style={{ color:'var(--text-secondary)' }}>{topic.topic}</span>
              <div className="flex-1 h-2 rounded-full overflow-hidden" style={{ background:'var(--bg-tertiary)' }}>
                <motion.div
                  initial={{ width:0 }} animate={{ width:`${topic.score}%` }} transition={{ duration:0.6, delay:0.4 }}
                  className="h-full rounded-full"
                  style={{ background: topic.score>=75 ? 'var(--success)' : topic.score>=50 ? 'var(--accent)' : 'var(--warning)' }}
                />
              </div>
              <span className="text-xs font-mono w-14 text-right flex-shrink-0" style={{ color:'var(--text-muted)' }}>{topic.solved}/{topic.total}</span>
              <Badge variant={topic.score>=75?'success':topic.score>=50?'info':'warning'} size="sm">{topic.score}%</Badge>
            </div>
          ))}
        </div>
      </motion.div>

      {/* Recent submissions */}
      <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }} transition={{ delay:0.4 }}
        className="rounded-xl border" style={{ background:'var(--bg-card)', borderColor:'var(--border)' }}>
        <div className="px-4 py-3 border-b flex items-center gap-2" style={{ borderColor:'var(--border)' }}>
          <Clock size={13} style={{ color:'var(--accent)' }} />
          <p className="text-xs font-mono font-semibold" style={{ color:'var(--text-primary)' }}>RECENT SUBMISSIONS</p>
        </div>
        <div>
          {MOCK.recent.map((s, i) => (
            <div key={s.id} className={`flex items-center gap-3 px-4 py-3 ${i < MOCK.recent.length-1 ? 'border-b' : ''}`} style={{ borderColor:'var(--border)' }}>
              {s.status === 'accepted'
                ? <CheckCircle size={13} style={{ color:'var(--success)' }} className="flex-shrink-0" />
                : <AlertTriangle size={13} style={{ color:'var(--error)' }} className="flex-shrink-0" />}
              <span className="text-sm flex-1 font-medium" style={{ color:'var(--text-primary)' }}>{s.problem}</span>
              <Badge variant="accent" size="sm">{s.complexity}</Badge>
              <span className="text-xs font-mono w-12 text-center" style={{ color:'var(--text-muted)' }}>{s.language}</span>
              <span className="text-xs" style={{ color:'var(--text-muted)' }}>{s.time}</span>
            </div>
          ))}
        </div>
      </motion.div>

      {/* Weak areas */}
      <motion.div initial={{ opacity:0, y:16 }} animate={{ opacity:1, y:0 }} transition={{ delay:0.45 }}
        className="rounded-xl border p-4" style={{ background:'rgba(246,173,85,0.05)', borderColor:'var(--warning)' }}>
        <div className="flex items-center gap-2 mb-3">
          <TrendingUp size={13} style={{ color:'var(--warning)' }} />
          <p className="text-xs font-mono font-semibold" style={{ color:'var(--warning)' }}>SUGGESTED FOCUS AREAS</p>
        </div>
        <div className="flex flex-wrap gap-2">
          {['Dynamic Programming','Graph Traversal','Memoization','Dijkstra','Segment Trees'].map(t => (
            <span key={t} className="px-3 py-1 rounded-full text-xs border" style={{ background:'var(--bg-secondary)', borderColor:'var(--border)', color:'var(--text-secondary)' }}>
              {t}
            </span>
          ))}
        </div>
      </motion.div>
    </div>
  )
}
