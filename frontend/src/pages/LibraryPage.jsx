import { useEffect, useState } from 'react'
import { getUserGames, getUserGamesByStatus } from '../api/api'
import StatusCards from '../components/StatusCards'
import GameGrid from '../components/GameGrid'
import './LibraryPage.css'

export default function LibraryPage() {
    const [allGames, setAllGames] = useState([])
    const [filtered, setFiltered] = useState([])
    const [activeStatus, setActiveStatus] = useState(null)
    const [loading, setLoading] = useState(true)
    const [showProgress, setShowProgress] = useState(true)

    useEffect(() => {
        getUserGames().then(res => {
            setAllGames(res.data)
            setFiltered(res.data)
            setLoading(false)
        })
    }, [])

    const handleStatusSelect = async (status) => {
        setActiveStatus(status)
        if (status === null) {
            setFiltered(allGames)
        } else {
            const res = await getUserGamesByStatus(status)
            setFiltered(res.data)
        }
    }

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
            <GameGrid games={filtered} showProgress={showProgress} />
        </div>
    )
}