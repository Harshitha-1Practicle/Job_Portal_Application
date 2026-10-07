import { useDeferredValue, useEffect, useState } from 'react'
import {
  Activity,
  AlertCircle,
  ArrowDownRight,
  ArrowRight,
  ArrowUpRight,
  BriefcaseBusiness,
  Building2,
  CalendarDays,
  CheckCircle2,
  CircleHelp,
  Clock3,
  Compass,
  FileText,
  LayoutDashboard,
  LoaderCircle,
  LockKeyhole,
  LogOut,
  MapPin,
  Search,
  Send,
  Sparkles,
  X,
} from 'lucide-react'
import { Link, NavLink, Route, Routes, useLocation } from 'react-router-dom'
import { apiRequest, formatDate, getInitials, getSkills, humanizeStatus } from './api'
import './Dashboard.css'

const STATUS_FLOW = ['APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED']
const FILTERS = ['All roles', 'Full time', 'Internships', 'Remote']

export default function Dashboard({ session, onLogout, onSessionExpired }) {
  const [jobs, setJobs] = useState([])
  const [applications, setApplications] = useState([])
  const [jobsError, setJobsError] = useState('')
  const [applicationsError, setApplicationsError] = useState('')
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState('')
  const [applyJob, setApplyJob] = useState(null)
  const [applyBusy, setApplyBusy] = useState(false)
  const [applyError, setApplyError] = useState('')
  const location = useLocation()

  useEffect(() => {
    let active = true

    async function loadDashboard() {
      const [jobsResult, applicationsResult] = await Promise.allSettled([
        apiRequest('/api/jobs'),
        apiRequest('/api/applications', { token: session.token }),
      ])
      if (!active) return

      if (jobsResult.status === 'fulfilled') {
        setJobs(Array.isArray(jobsResult.value) ? jobsResult.value : [])
      } else {
        setJobsError(jobsResult.reason.message)
      }

      if (applicationsResult.status === 'fulfilled') {
        setApplications(Array.isArray(applicationsResult.value) ? applicationsResult.value : [])
      } else {
        setApplicationsError(applicationsResult.reason.message)
        if (applicationsResult.reason.status === 401 || applicationsResult.reason.status === 403) onSessionExpired()
      }
      setLoading(false)
    }

    loadDashboard()
    return () => { active = false }
  }, [session.token, onSessionExpired])

  useEffect(() => {
    if (!toast) return undefined
    const timeout = window.setTimeout(() => setToast(''), 3800)
    return () => window.clearTimeout(timeout)
  }, [toast])

  async function handleApply(values) {
    setApplyBusy(true)
    setApplyError('')
    try {
      await apiRequest('/api/applications', {
        method: 'POST',
        token: session.token,
        body: JSON.stringify({
          jobId: Number(applyJob.id),
          resume: values.resume.trim() || null,
          coverLetter: values.coverLetter.trim() || null,
        }),
      })
      setApplyJob(null)
      setToast('Application submitted. It’s now in your tracker.')
      const updatedApplications = await apiRequest('/api/applications', { token: session.token })
      setApplications(Array.isArray(updatedApplications) ? updatedApplications : [])
    } catch (error) {
      if (error.status === 401 || error.status === 403) onSessionExpired()
      setApplyError(error.message)
    } finally {
      setApplyBusy(false)
    }
  }

  const firstName = session.email?.split('@')[0]?.split(/[._-]/)[0] ?? 'there'
  const pageName = location.pathname.endsWith('/jobs')
    ? 'Find roles'
    : location.pathname.endsWith('/applications')
      ? 'Applications'
      : 'Overview'

  return (
    <div className="dashboard-shell">
      <aside className="dashboard-sidebar">
        <Link className="dashboard-brand" to="/dashboard" aria-label="Folio dashboard">
          <span className="dashboard-brand-mark"><Sparkles size={17} /></span><span>folio<span className="brand-period">.</span></span>
        </Link>
        <div className="dashboard-workspace-label">WORKSPACE</div>
        <nav className="dashboard-nav" aria-label="Dashboard navigation">
          <NavLink to="/dashboard" end className={({ isActive }) => `dashboard-nav-link ${isActive ? 'active' : ''}`}><LayoutDashboard size={17} />Overview</NavLink>
          <NavLink to="/dashboard/jobs" className={({ isActive }) => `dashboard-nav-link ${isActive ? 'active' : ''}`}><Compass size={17} />Find roles</NavLink>
          <NavLink to="/dashboard/applications" className={({ isActive }) => `dashboard-nav-link ${isActive ? 'active' : ''}`}><FileText size={17} />Applications{applications.length > 0 && <span className="dashboard-nav-count">{applications.length}</span>}</NavLink>
        </nav>
        <div className="dashboard-sidebar-bottom">
          <span className="dashboard-avatar">{getInitials(session.email)}</span>
          <span className="dashboard-account"><strong>{session.email}</strong><small>Job seeker account</small></span>
          <button className="dashboard-icon-button" onClick={onLogout} title="Sign out" aria-label="Sign out"><LogOut size={16} /></button>
        </div>
      </aside>

      <main className="dashboard-main">
        <header className="dashboard-topbar">
          <div className="dashboard-breadcrumb"><span>Folio</span><span className="crumb-slash">/</span><strong>{pageName}</strong></div>
          <div className="dashboard-topbar-right"><span className="dashboard-live"><i />Connected</span><button className="dashboard-help" title="Your data comes from your Folio account"><CircleHelp size={17} /></button></div>
        </header>
        <div className="dashboard-content">
          <Routes>
            <Route index element={<OverviewPage firstName={firstName} jobs={jobs} applications={applications} jobsError={jobsError} applicationsError={applicationsError} loading={loading} />} />
            <Route path="jobs" element={<JobsPage jobs={jobs} loading={loading} error={jobsError} onApply={setApplyJob} />} />
            <Route path="applications" element={<ApplicationsPage applications={applications} loading={loading} error={applicationsError} />} />
            <Route path="*" element={<OverviewPage firstName={firstName} jobs={jobs} applications={applications} jobsError={jobsError} applicationsError={applicationsError} loading={loading} />} />
          </Routes>
        </div>
      </main>

      {applyJob && <ApplyDialog job={applyJob} busy={applyBusy} error={applyError} onClose={() => { setApplyJob(null); setApplyError('') }} onSubmit={handleApply} />}
      {toast && <div className="dashboard-toast" role="status"><CheckCircle2 size={17} />{toast}</div>}
    </div>
  )
}

function OverviewPage({ firstName, jobs, applications, jobsError, applicationsError, loading }) {
  const recentApplications = [...applications].sort((first, second) => new Date(second.appliedAt) - new Date(first.appliedAt)).slice(0, 4)
  const statuses = STATUS_FLOW.map((status) => ({
    status,
    count: applications.filter((application) => (application.status ?? 'APPLIED').toUpperCase() === status).length,
  }))
  const maxCount = Math.max(...statuses.map((item) => item.count), 1)

  return (
    <section className="overview-page">
      <div className="overview-heading">
        <div><p className="dashboard-eyebrow">YOUR JOB SEARCH, AT A GLANCE</p><h1>Good morning, <span>{firstName}.</span></h1><p>Pick one useful next step. The rest is already taking shape.</p></div>
        <div className="overview-date"><CalendarDays size={15} />{new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' })}</div>
      </div>

      <div className="dashboard-stats">
        <MetricCard label="Applications sent" value={loading ? '—' : applications.length} detail="All submissions in your tracker" icon={<Send size={17} />} tone="green" />
        <MetricCard label="In progress" value={loading ? '—' : applications.filter((item) => !['SELECTED', 'REJECTED'].includes((item.status ?? 'APPLIED').toUpperCase())).length} detail="Waiting on the next update" icon={<Activity size={17} />} tone="lime" />
        <MetricCard label="Interviews" value={loading ? '—' : applications.filter((item) => (item.status ?? '').toUpperCase() === 'INTERVIEW').length} detail="Roles at interview stage" icon={<CalendarDays size={17} />} tone="coral" />
        <MetricCard label="Open roles" value={loading ? '—' : jobs.length} detail="Available in your job board" icon={<BriefcaseBusiness size={17} />} tone="blue" />
      </div>

      <div className="overview-grid">
        <section className="dashboard-panel pipeline-panel">
          <div className="panel-heading"><div><p className="panel-overline">REAL-TIME STATUS</p><h2>Application pipeline</h2></div><span className="panel-total">{applications.length} total</span></div>
          {applicationsError ? <InlineError message={applicationsError} /> : applications.length ? (
            <div className="pipeline-list">
              {statuses.map(({ status, count }) => (
                <div className="pipeline-row" key={status}>
                  <span className={`pipeline-marker marker-${status.toLowerCase()}`} />
                  <span className="pipeline-label">{humanizeStatus(status)}</span>
                  <div className="pipeline-track"><span style={{ width: `${(count / maxCount) * 100}%` }} /></div>
                  <strong>{count}</strong>
                </div>
              ))}
            </div>
          ) : loading ? <LoadingLine label="Loading application history" /> : <EmptyNotice title="Your pipeline starts here" copy="Apply to a role and it will appear with its current status." />}
          {!applicationsError && <p className="panel-footnote"><span />Counts come from your saved application records.</p>}
        </section>

        <section className="dashboard-panel next-step-panel">
          <div className="panel-heading"><div><p className="panel-overline">A GOOD NEXT STEP</p><h2>Keep your search moving</h2></div><span className="next-step-spark"><Sparkles size={16} /></span></div>
          <p className="next-step-copy">{applications.length === 0 ? 'You have a clean slate. Explore open roles and start building your application history.' : `You have ${applications.length} ${applications.length === 1 ? 'application' : 'applications'} in your tracker. Check for updates, then find another role that fits.`}</p>
          <div className="next-step-number"><strong>{jobs.length.toString().padStart(2, '0')}</strong><span>current openings<br />from your workspace</span></div>
          {jobsError && <InlineError message={jobsError} />}
          <Link className="dashboard-primary-link" to="/dashboard/jobs">Browse open roles <ArrowRight size={16} /></Link>
        </section>

        <section className="dashboard-panel recent-panel">
          <div className="panel-heading"><div><p className="panel-overline">YOUR RECENT ACTIVITY</p><h2>Latest applications</h2></div><Link className="text-link" to="/dashboard/applications">View all <ArrowRight size={14} /></Link></div>
          {applicationsError ? <InlineError message={applicationsError} /> : recentApplications.length ? (
            <div className="recent-list">
              {recentApplications.map((application) => <ApplicationRow key={application.id} application={application} />)}
            </div>
          ) : loading ? <LoadingLine label="Loading your activity" /> : <EmptyNotice title="Nothing submitted yet" copy="Your applications and their status updates will appear here." />}
        </section>

        <section className="dashboard-panel recommended-panel">
          <div className="panel-heading"><div><p className="panel-overline">FROM THE LIVE JOB BOARD</p><h2>Roles to explore</h2></div><Link className="text-link" to="/dashboard/jobs">See all <ArrowRight size={14} /></Link></div>
          {jobsError ? <InlineError message={jobsError} /> : jobs.length ? (
            <div className="recommended-list">
              {jobs.slice(0, 3).map((job, index) => <RecommendedJob key={job.id} job={job} index={index} />)}
            </div>
          ) : loading ? <LoadingLine label="Loading open roles" /> : <EmptyNotice title="No roles are listed yet" copy="Check back when new opportunities are posted." />}
        </section>
      </div>
    </section>
  )
}

function MetricCard({ label, value, detail, icon, tone }) {
  return <article className={`metric-card metric-${tone}`}><div className="metric-top"><span>{label}</span><span className="metric-icon">{icon}</span></div><strong className="metric-value">{value}</strong><span className="metric-detail">{detail}</span></article>
}

function JobsPage({ jobs, loading, error, onApply }) {
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState('All roles')
  const [selectedId, setSelectedId] = useState(null)
  const deferredQuery = useDeferredValue(query.trim().toLowerCase())
  const filteredJobs = jobs.filter((job) => {
    const searchText = [job.title, job.company, job.location, job.skills, job.description].filter(Boolean).join(' ').toLowerCase()
    const kind = (job.jobType ?? '').toLowerCase()
    const location = (job.location ?? '').toLowerCase()
    const matchesFilter = filter === 'All roles'
      || (filter === 'Full time' && kind.includes('full'))
      || (filter === 'Internships' && kind.includes('intern'))
      || (filter === 'Remote' && location.includes('remote'))
    return searchText.includes(deferredQuery) && matchesFilter
  })
  const selectedJob = filteredJobs.find((job) => job.id === selectedId) ?? filteredJobs[0] ?? null

  return (
    <section className="jobs-page">
      <div className="dashboard-page-heading"><div><p className="dashboard-eyebrow">A LIVE LOOK AT WHAT’S OPEN</p><h1>Find your next role.</h1><p>Search real opportunities from your Folio workspace.</p></div><div className="heading-open-count"><strong>{jobs.length.toString().padStart(2, '0')}</strong><span>open roles</span></div></div>
      <div className="job-search-toolbar"><label className="dashboard-search"><Search size={18} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search title, company, location, or skill" aria-label="Search jobs" /><span>{filteredJobs.length} results</span></label><div className="job-filter-row">{FILTERS.map((option) => <button key={option} className={`job-filter ${filter === option ? 'selected' : ''}`} onClick={() => setFilter(option)} aria-pressed={filter === option}>{option}</button>)}</div></div>
      {error ? <InlineError message={error} /> : loading ? <LoadingLine label="Loading live job listings" /> : filteredJobs.length ? (
        <div className="jobs-workspace">
          <div className="jobs-results"><p className="results-caption">MATCHING OPENINGS <span>{filteredJobs.length}</span></p>{filteredJobs.map((job, index) => <button key={job.id} onClick={() => setSelectedId(job.id)} className={`dashboard-job-card ${selectedJob?.id === job.id ? 'selected' : ''}`} aria-pressed={selectedJob?.id === job.id}><span className={`job-company-monogram monogram-${index % 4}`}>{getInitials(job.company)}</span><span className="dashboard-job-summary"><span className="dashboard-company-name">{job.company}</span><strong>{job.title}</strong><span className="dashboard-job-meta"><span><MapPin size={13} />{job.location || 'Location flexible'}</span><span>{job.jobType || 'Open role'}</span></span></span><ArrowUpRight size={16} /></button>)}</div>
          {selectedJob && <JobDetailPanel job={selectedJob} onApply={() => onApply(selectedJob)} />}
        </div>
      ) : <EmptyNotice title="No matches this time" copy="Try another keyword or clear the selected filters." />}
    </section>
  )
}

function JobDetailPanel({ job, onApply }) {
  const skills = getSkills(job.skills ?? '')
  return <article className="job-detail-panel"><div className="job-detail-top"><span className="job-open-state"><i />OPEN POSITION</span><span className="job-category">{job.jobType || 'Opportunity'}</span></div><div className="job-company-lockup"><span><Building2 size={20} /></span><strong>{job.company}</strong></div><h2>{job.title}</h2><div className="job-detail-meta"><span><MapPin size={14} />{job.location || 'Location flexible'}</span>{job.experience && <span><Clock3 size={14} />{job.experience}</span>}</div>{job.salary && <div className="job-salary"><span>COMPENSATION</span><strong>{job.salary}</strong></div>}<JobDescription title="About the role" copy={job.description} />{job.responsibilities && <JobDescription title="What you’ll do" copy={job.responsibilities} />}{job.qualifications && <JobDescription title="What you bring" copy={job.qualifications} />}{skills.length > 0 && <div className="job-detail-section"><h3>Skills</h3><div className="job-skills">{skills.map((skill) => <span key={skill}>{skill}</span>)}</div></div>}{job.benefits && <JobDescription title="Benefits" copy={job.benefits} />}<button className="dashboard-apply-button" onClick={onApply}>Apply to this role <ArrowRight size={16} /></button></article>
}

function JobDescription({ title, copy }) {
  return <div className="job-detail-section"><h3>{title}</h3><p>{copy}</p></div>
}

function ApplicationsPage({ applications, loading, error }) {
  const sortedApplications = [...applications].sort((first, second) => new Date(second.appliedAt) - new Date(first.appliedAt))
  const applied = applications.filter((item) => (item.status ?? 'APPLIED').toUpperCase() === 'APPLIED').length
  const progressed = applications.length - applied
  return (
    <section className="applications-page">
      <div className="dashboard-page-heading"><div><p className="dashboard-eyebrow">YOUR JOB SEARCH, ORGANIZED</p><h1>Application tracker.</h1><p>Follow each real submission from your account.</p></div><div className="heading-open-count"><strong>{applications.length.toString().padStart(2, '0')}</strong><span>submissions</span></div></div>
      <div className="application-summary-strip"><span><FileText size={16} />{applied} newly submitted</span><span><Activity size={16} />{progressed} with a status update</span><span><CheckCircle2 size={16} />{applications.filter((item) => (item.status ?? '').toUpperCase() === 'SELECTED').length} selected</span></div>
      {error ? <InlineError message={error} /> : loading ? <LoadingLine label="Loading your application records" /> : sortedApplications.length ? <div className="tracker-table"><div className="tracker-table-head"><span>APPLICATION</span><span>SUBMITTED</span><span>STATUS</span></div>{sortedApplications.map((application) => <ApplicationRow key={application.id} application={application} detailed />)}</div> : <EmptyNotice title="Your tracker is ready" copy="When you apply to a role, the submission and status will appear here." />}
    </section>
  )
}

function ApplicationRow({ application, detailed = false }) {
  const status = (application.status ?? 'APPLIED').toUpperCase()
  const job = application.job
  return <article className={`dashboard-application-row ${detailed ? 'detailed' : ''}`}><span className="application-row-icon"><FileText size={17} /></span><span className="application-row-main"><strong>{job?.title ?? `Application #${application.id}`}</strong><small>{job?.company ?? `Submission #${application.id}`}</small></span><span className="application-date"><CalendarDays size={13} />{formatDate(application.appliedAt)}</span><span className={`application-status status-${status.toLowerCase()}`}>{humanizeStatus(status)}</span></article>
}

function RecommendedJob({ job, index }) {
  return <Link className="recommended-job" to="/dashboard/jobs"><span className={`recommended-monogram monogram-${index % 4}`}>{getInitials(job.company)}</span><span><strong>{job.title}</strong><small>{job.company} · {job.location}</small></span><ArrowUpRight size={15} /></Link>
}

function ApplyDialog({ job, busy, error, onClose, onSubmit }) {
  function submit(event) {
    event.preventDefault()
    onSubmit(Object.fromEntries(new FormData(event.currentTarget).entries()))
  }
  return <div className="dashboard-modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget && !busy) onClose() }}><section className="dashboard-modal" role="dialog" aria-modal="true" aria-labelledby="apply-heading"><button className="dashboard-modal-close" onClick={onClose} aria-label="Close" disabled={busy}><X size={18} /></button><span className="dashboard-modal-icon"><Send size={18} /></span><p className="panel-overline">APPLICATION</p><h2 id="apply-heading">Apply to {job.title}</h2><p className="modal-company-line">{job.company} · {job.location}</p><form onSubmit={submit} className="apply-form"><label>Resume link <span>OPTIONAL</span><input type="url" name="resume" placeholder="https://your-resume-or-portfolio.pdf" /></label><label>Note to the hiring team <span>OPTIONAL</span><textarea name="coverLetter" rows="5" maxLength="2000" placeholder="Share why this role feels like a good fit." /></label>{error && <p className="dashboard-form-error" role="alert"><AlertCircle size={15} />{error}</p>}<button className="dashboard-apply-button" disabled={busy}>{busy ? <LoaderCircle className="dashboard-spin" size={16} /> : null}{busy ? 'Submitting…' : 'Submit application'}{!busy && <ArrowRight size={16} />}</button></form><p className="modal-footnote"><LockKeyhole size={13} />Your application is tied to your signed-in account.</p></section></div>
}

function InlineError({ message }) {
  return <div className="dashboard-inline-error"><AlertCircle size={16} /><span>{message}</span></div>
}

function LoadingLine({ label }) {
  return <div className="dashboard-loading"><LoaderCircle className="dashboard-spin" size={18} />{label}</div>
}

function EmptyNotice({ title, copy }) {
  return <div className="dashboard-empty"><span><ArrowDownRight size={18} /></span><strong>{title}</strong><p>{copy}</p></div>
}
