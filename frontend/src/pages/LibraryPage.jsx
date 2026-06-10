import { useEffect, useRef, useState } from 'react'
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
        let aVal, bVal

        if (sortBy === 'title') {
            aVal = a.title.toLowerCase()
            bVal = b.title.toLowerCase()
            return sortDir === 'asc'
                ? aVal.localeCompare(bVal)
                : bVal.localeCompare(aVal)
        }

        if (sortBy === 'percentage') {
            aVal = a.totalAchievements > 0
                ? a.unlockedAchievements / a.totalAchievements
                : -1
            bVal = b.totalAchievements > 0
                ? b.unlockedAchievements / b.totalAchievements
                : -1
        }

        if (sortBy === 'lastPlayedAt') {
            aVal = a.lastPlayedAt ? new Date(a.lastPlayedAt).getTime() : 0
            bVal = b.lastPlayedAt ? new Date(b.lastPlayedAt).getTime() : 0
        }

        return sortDir === 'asc' ? aVal - bVal : bVal - aVal
    })
}

export default function LibraryPage() {
    // ← all hooks must be here, inside the function
    const [allGames, setAllGames] = useState([])
    const [filtered, setFiltered] = useState([])
    const [activeStatus, setActiveStatus] = useState(null)
    const [loading, setLoading] = useState(true)
    const [showProgress, setShowProgress] = useState(true)
    const [search, setSearch] = useState('')
    const [sortBy, setSortBy] = useState('title')
    const [sortDir, setSortDir] = useState('asc')
    const [sortOpen, setSortOpen] = useState(false)
    const sortRef = useRef(null)  // ← must be inside here

    useEffect(() => {
        getUserGames().then(res => {
            setAllGames(res.data)
            setFiltered(res.data)
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

    const handleStatusSelect = async (status) => {
        setActiveStatus(status)
        setSearch('')
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
        const base = activeStatus === null
            ? allGames
            : allGames.filter(g => g.status === activeStatus)
        const searched = base.filter(g =>
            g.title.toLowerCase().includes(value.toLowerCase())
        )
        setFiltered(sortGames(searched, sortBy, sortDir))
    }

    const handleStatusChange = (userGameId, newStatus) => {
        // Update allGames
        setAllGames(prev => prev.map(g =>
            g.id === userGameId ? { ...g, status: newStatus } : g
        ))

        // Update filtered — if a status filter is active, remove the game if it no longer matches
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
                    <button
                        className="sort-dir-btn"
                        onClick={() => setSortDir(d => d === 'asc' ? 'desc' : 'asc')}
                    >
                        {sortDir === 'asc' ? '↑' : '↓'}
                    </button>
                    {sortOpen && (
                        <div className="sort-menu">
                            {SORT_OPTIONS.map(o => (
                                <button
                                    key={o.value}
                                    className={`sort-option ${sortBy === o.value ? 'active' : ''}`}
                                    onClick={() => {
                                        setSortBy(o.value)
                                        setSortOpen(false)
                                    }}
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