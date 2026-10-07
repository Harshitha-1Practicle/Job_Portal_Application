import { useEffect, useState } from 'react'
import { ArrowRight, BriefcaseBusiness, Check, Eye, EyeOff, LockKeyhole, Mail, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiRequest } from './api'
import './AuthPage.css'

export default function AuthPage({ mode, onAuthenticated }) {
  const registering = mode === 'register'
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [openRoles, setOpenRoles] = useState(null)
  const [featureJob, setFeatureJob] = useState(null)

  useEffect(() => {
    let active = true
    apiRequest('/api/jobs')
      .then((jobs) => {
        if (!active || !Array.isArray(jobs)) return
        setOpenRoles(jobs.length)
        setFeatureJob(jobs[0] ?? null)
      })
      .catch(() => {
        if (active) setOpenRoles(null)
      })
    return () => { active = false }
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    const values = Object.fromEntries(new FormData(event.currentTarget).entries())
    if (registering && values.password !== values.confirmPassword) {
      setError('Those passwords do not match.')
      return
    }

    setBusy(true)
    try {
      const result = await apiRequest(`/api/auth/${registering ? 'register' : 'login'}`, {
        method: 'POST',
        body: JSON.stringify(registering
          ? { name: values.name.trim(), email: values.email.trim().toLowerCase(), password: values.password, role: 'ROLE_JOB_SEEKER' }
          : { email: values.email.trim().toLowerCase(), password: values.password }),
      })
      onAuthenticated({ token: result.token, email: result.email, role: result.role })
    } catch (requestError) {
      if (requestError instanceof TypeError) {
        setError('We could not reach Folio. Check your connection and try again.')
      } else if (requestError.status === 401) {
        setError('That email and password do not match. Check them and try again.')
      } else if (requestError.status === 409) {
        setError('An account with this email already exists. Sign in instead.')
      } else if (requestError.status >= 500) {
        setError('Something went wrong on our end. Please try again in a moment.')
      } else {
        setError(requestError.message || 'We could not complete your request. Please try again.')
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-story" aria-label="Folio job search workspace">
        <img
          className="auth-story-image"
          src="https://images.unsplash.com/photo-1521737711867-e3b97375f902?auto=format&fit=crop&w=1800&q=85"
          alt="A team sharing ideas around a table"
        />
        <div className="auth-story-wash" />
        <Link className="auth-brand" to="/" aria-label="Folio home">
          <span className="auth-brand-mark"><Sparkles size={17} /></span>
          <span>folio<span>.</span></span>
        </Link>
        <div className="story-content">
          <p className="story-eyebrow"><span /> MAKE YOUR NEXT MOVE</p>
          <h1>Good work<br />starts with<br /><em>your kind of</em><br />ambition.</h1>
          <p className="story-description">A calmer place to discover meaningful roles and keep your search moving forward.</p>
          {featureJob && (
            <div className="featured-opening">
              <span className="featured-icon"><BriefcaseBusiness size={17} /></span>
              <span className="featured-copy"><span>LIVE OPPORTUNITY</span><strong>{featureJob.title}</strong><small>{featureJob.company} · {featureJob.location}</small></span>
              <ArrowRight size={17} />
            </div>
          )}
          <div className="story-foot">
            <div className="story-stat"><strong>{openRoles === null ? '—' : openRoles.toString().padStart(2, '0')}</strong><span>open roles<br />from your API</span></div>
            <div className="story-stat-divider" />
            <p><span className="live-pulse" />{openRoles === null ? 'Connecting to your workspace' : 'Fresh opportunities, live from your workspace'}</p>
          </div>
        </div>
        <span className="story-legal">FOLIO CAREER WORKSPACE · {new Date().getFullYear()}</span>
      </section>

      <section className="auth-panel">
        <div className="auth-panel-top">
          <span>{registering ? 'Already have a workspace?' : 'New to Folio?'}</span>
          <Link to={registering ? '/sign-in' : '/register'}>{registering ? 'Sign in' : 'Create account'} <ArrowRight size={14} /></Link>
        </div>
        <div className="auth-form-wrap">
          <p className="auth-kicker">{registering ? 'YOUR NEXT CHAPTER' : 'WELCOME BACK'}</p>
          <h2>{registering ? 'Make space for better work.' : 'Pick up where you left off.'}</h2>
          <p className="auth-subtitle">{registering ? 'Create your job seeker account. Your applications stay organized from day one.' : 'Sign in to explore roles and see how your applications are moving.'}</p>

          <div className="auth-mode-switch" aria-label="Account access">
            <Link className={!registering ? 'selected' : ''} to="/sign-in">Sign in</Link>
            <Link className={registering ? 'selected' : ''} to="/register">Create account</Link>
          </div>

          <form className="auth-form" onSubmit={handleSubmit} aria-busy={busy}>
            {registering && (
              <label className="auth-label" htmlFor="auth-name">Full name
                <input id="auth-name" name="name" type="text" autoComplete="name" placeholder="Your name" maxLength="120" required />
              </label>
            )}
            <label className="auth-label" htmlFor="auth-email">Email address
              <span className="auth-input-icon"><Mail size={17} aria-hidden="true" /><input id="auth-email" name="email" type="email" autoComplete="email" autoCapitalize="none" spellCheck="false" placeholder="name@example.com" required /></span>
            </label>
            <label className="auth-label" htmlFor="auth-password">Password
              <span className="auth-input-icon"><LockKeyhole size={17} aria-hidden="true" /><input id="auth-password" name="password" type={showPassword ? 'text' : 'password'} autoComplete={registering ? 'new-password' : 'current-password'} minLength={registering ? 6 : undefined} placeholder={registering ? 'At least 6 characters' : 'Enter your password'} required />
                <button className="auth-password-toggle" type="button" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? 'Hide password' : 'Show password'} title={showPassword ? 'Hide password' : 'Show password'}>
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </span>
            </label>
            {registering && (
              <label className="auth-label" htmlFor="auth-confirm-password">Confirm password
                <span className="auth-input-icon"><LockKeyhole size={17} aria-hidden="true" /><input id="auth-confirm-password" name="confirmPassword" type={showConfirmPassword ? 'text' : 'password'} autoComplete="new-password" minLength="6" placeholder="Enter it once more" required />
                  <button className="auth-password-toggle" type="button" onClick={() => setShowConfirmPassword((visible) => !visible)} aria-label={showConfirmPassword ? 'Hide confirmation password' : 'Show confirmation password'} title={showConfirmPassword ? 'Hide confirmation password' : 'Show confirmation password'}>
                    {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </span>
              </label>
            )}
            {error && <p className="auth-error" id="auth-error" role="alert" aria-live="assertive">{error}</p>}
            <button className="auth-submit" type="submit" disabled={busy}>
              {busy ? 'Please wait…' : registering ? 'Create my account' : 'Sign in'}
              {busy ? <span className="button-spinner" aria-hidden="true" /> : <ArrowRight size={17} aria-hidden="true" />}
            </button>
          </form>

          {registering ? (
            <p className="auth-assurance"><Check size={14} /> Job seeker account · No recruiter access is granted at sign-up</p>
          ) : (
            <p className="auth-assurance"><LockKeyhole size={13} /> Your account details are sent securely to your workspace.</p>
          )}
          {!registering && <p className="auth-bottom-link">New around here? <Link to="/register">Create a free account</Link></p>}
        </div>
        <div className="auth-panel-footer"><span>Folio</span><span>Make your search count.</span></div>
      </section>
    </main>
  )
}
