import { useEffect, useState } from 'react'
import { getUserGames, getUserGamesByStatus } from '../api/api.js'
import StatusCards from '../components/StatusCards.jsx'
import GameGrid from '../components/GameGrid.jsx'
import './LibraryPage.css'

export default function LibraryPage() {
    const [allGames, setAllGames] = useState([])
    const [filtered, setFiltered] = useState([])
    const [activeStatus, setActiveStatus] = useState(null)
    const [loading, setLoading] = useState(true)

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

    const counts = allGames.reduce((acc, ug) => {
        acc[ug.status] = (acc[ug.status] || 0) + 1
        acc[null] = allGames.length
        return acc
    }, { null: 0 })

    if (loading) return <div className="loading">Loading library...</div>

    return (
        <div>
            <StatusCards
                active={activeStatus}
                onSelect={handleStatusSelect}
                counts={counts}
            />
            <GameGrid games={filtered} />
        </div>
    )
}