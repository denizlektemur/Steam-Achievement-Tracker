import { useEffect, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getUserGames, getUserGamesByStatus } from '../api/api'
import StatusCards from '../components/StatusCards'
import GameGrid from '../components/GameGrid'
import './LibraryPage.css'

const SORT_OPTIONS = [
    { value: 'title', label: 'Name' },
    { value: 'percentage', label: 'Completion %' },
    { value: 'lastPlayedAt', label: 'Last Played' },
]

function sortGames(games, sortBy, sortDir) {
    return [...games].sort((a, b) => {
        if (sortBy === 'title') {
            const aVal = a.title.toLowerCase()
            const bVal = b.title.toLowerCase()
            return sortDir === 'asc'
                ? aVal.localeCompare(bVal)
                : bVal.localeCompare(aVal)
        }

        let aVal, bVal

        if (sortBy === 'percentage') {
            aVal = a.totalAchievements > 0
                ? a.unlockedAchievements / a.totalAchievements : -1
            bVal = b.totalAchievements > 0
                ? b.unlockedAchievements / b.totalAchievements : -1
        }

        if (sortBy === 'lastPlayedAt') {
            aVal = a.lastPlayedAt ? new Date(a.lastPlayedAt).getTime() : 0
            bVal = b.lastPlayedAt ? new Date(b.lastPlayedAt).getTime() : 0
        }

        return sortDir === 'asc' ? aVal - bVal : bVal - aVal
    })
}

export default function LibraryPage() {
    const [searchParams, setSearchParams] = useSearchParams()

    // Read initial state from URL params
    const initialStatus = searchParams.get('status') || null
    const initialSort = searchParams.get('sort') || 'title'
    const initialDir = searchParams.get('dir') || 'asc'
    const initialSearch = searchParams.get('search') || ''

    const [allGames, setAllGames] = useState([])
    const [filtered, setFiltered] = useState([])
    const [activeStatus, setActiveStatus] = useState(initialStatus)
    const [loading, setLoading] = useState(true)
    const [showProgress, setShowProgress] = useState(true)
    const [search, setSearch] = useState(initialSearch)
    const [sortBy, setSortBy] = useState(initialSort)
    const [sortDir, setSortDir] = useState(initialDir)
    const [sortOpen, setSortOpen] = useState(false)
    const sortRef = useRef(null)

    // Update URL when filters change
    const updateParams = (updates) => {
        setSearchParams(prev => {
            const next = new URLSearchParams(prev)
            Object.entries(updates).forEach(([key, value]) => {
                if (value === null || value === '') {
                    next.delete(key)
                } else {
                    next.set(key, value)
                }
            })
            return next
        }, { replace: true })
    }

    useEffect(() => {
        getUserGames().then(res => {
            setAllGames(res.data)

            // Apply initial filters from URL
            let base = res.data
            if (initialStatus) {
                base = base.filter(g => g.status === initialStatus)
            }
            if (initialSearch) {
                base = base.filter(g =>
                    g.title.toLowerCase().includes(initialSearch.toLowerCase())
                )
            }
            setFiltered(sortGames(base, initialSort, initialDir))
            setLoading(false)
        })
    }, [])

    useEffect(() => {
        setFiltered(prev => sortGames(prev, sortBy, sortDir))
    }, [sortBy, sortDir])

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (sortRef.current && !sortRef.current.contains(e.target)) {
                setSortOpen(false)
            }
        }
        document.addEventListener('mousedown', handleClickOutside)
        return () => document.removeEventListener('mousedown', handleClickOutside)
    }, [])

    useEffect(() => {
        if (!loading) {
            const savedScroll = sessionStorage.getItem('libraryScrollY')
            if (savedScroll) {
                // Small timeout to let the grid render first
                setTimeout(() => {
                    window.scrollTo({ top: parseInt(savedScroll), behavior: 'instant' })
                    sessionStorage.removeItem('libraryScrollY')
                }, 50)
            }
        }
    }, [loading])

    const handleStatusSelect = async (status) => {
        setActiveStatus(status)
        setSearch('')
        updateParams({ status, search: null })

        let base
        if (status === null) {
            base = allGames
        } else {
            const res = await getUserGamesByStatus(status)
            base = res.data
        }
        setFiltered(sortGames(base, sortBy, sortDir))
    }

    const handleSearch = (e) => {
        const value = e.target.value
        setSearch(value)
        updateParams({ search: value })

        const base = activeStatus === null
            ? allGames
            : allGames.filter(g => g.status === activeStatus)
        const searched = base.filter(g =>
            g.title.toLowerCase().includes(value.toLowerCase())
        )
        setFiltered(sortGames(searched, sortBy, sortDir))
    }

    const handleSortBy = (value) => {
        setSortBy(value)
        updateParams({ sort: value })
        setSortOpen(false)
    }

    const handleSortDir = () => {
        const newDir = sortDir === 'asc' ? 'desc' : 'asc'
        setSortDir(newDir)
        updateParams({ dir: newDir })
    }

    const handleStatusChange = (userGameId, newStatus) => {
        setAllGames(prev => prev.map(g =>
            g.id === userGameId ? { ...g, status: newStatus } : g
        ))
        setFiltered(prev => {
            const updated = prev.map(g =>
                g.id === userGameId ? { ...g, status: newStatus } : g
            )
            if (activeStatus !== null) {
                return updated.filter(g => g.status === activeStatus)
            }
            return updated
        })
    }

    const currentSortLabel = SORT_OPTIONS.find(o => o.value === sortBy)?.label

    const counts = {
        null: allGames.length,
        BACKLOG: allGames.filter(g => g.status === 'BACKLOG').length,
        IN_PROGRESS: allGames.filter(g => g.status === 'IN_PROGRESS').length,
        COMPLETED: allGames.filter(g => g.status === 'COMPLETED').length,
        IGNORED: allGames.filter(g => g.status === 'IGNORED').length,
    }

    if (loading) return <div className="loading">Loading library...</div>

    return (
        <div>
            <StatusCards
                active={activeStatus}
                onSelect={handleStatusSelect}
                counts={counts}
                showProgress={showProgress}
                onToggleProgress={() => setShowProgress(p => !p)}
            />
            <div className="library-search">
                <input
                    className="search-input"
                    placeholder="Search games..."
                    value={search}
                    onChange={handleSearch}
                />
                {search && (
                    <span className="search-results">
            {filtered.length} result{filtered.length !== 1 ? 's' : ''}
          </span>
                )}
                <div className="sort-split" ref={sortRef}>
                    <button
                        className={`sort-type-btn ${sortOpen ? 'open' : ''}`}
                        onClick={() => setSortOpen(o => !o)}
                    >
                        Sort: {currentSortLabel}
                    </button>
                    <button className="sort-dir-btn" onClick={handleSortDir}>
                        {sortDir === 'asc' ? '↑' : '↓'}
                    </button>
                    {sortOpen && (
                        <div className="sort-menu">
                            {SORT_OPTIONS.map(o => (
                                <button
                                    key={o.value}
                                    className={`sort-option ${sortBy === o.value ? 'active' : ''}`}
                                    onClick={() => handleSortBy(o.value)}
                                >
                                    {o.label}
                                    {sortBy === o.value && (
                                        <span className="sort-dir-indicator">
                      {sortDir === 'asc' ? '↑' : '↓'}
                    </span>
                                    )}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </div>
            <GameGrid
                games={filtered}
                showProgress={showProgress}
                onStatusChange={handleStatusChange}
            />
        </div>
    )
}