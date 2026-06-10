import GameCard from './GameCard'
import './GameGrid.css'

export default function GameGrid({ games, showProgress, onStatusChange }) {
    if (games.length === 0) {
        return <p className="empty">No games found.</p>
    }

    return (
        <div className="game-grid">
            {games.map(ug => (
                <GameCard
                    key={ug.id}
                    userGame={ug}
                    showProgress={showProgress}
                    onStatusChange={onStatusChange}
                />
            ))}
        </div>
    )
}