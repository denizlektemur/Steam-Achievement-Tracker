import GameCard from './GameCard'
import './GameGrid.css'

export default function GameGrid({ games }) {
    if (games.length === 0) {
        return <p className="empty">No games found.</p>
    }

    return (
        <div className="game-grid">
            {games.map(ug => (
                <GameCard key={ug.id} userGame={ug} />
            ))}
        </div>
    )
}