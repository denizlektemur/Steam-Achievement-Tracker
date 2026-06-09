import { useNavigate } from 'react-router-dom'
import './GameCard.css'

export default function GameCard({ userGame }) {
    const navigate = useNavigate()
    const { game } = userGame
    const imageUrl = `https://cdn.cloudflare.steamstatic.com/steam/apps/${game.appId}/library_600x900.jpg`
    const fallback = `https://cdn.cloudflare.steamstatic.com/steam/apps/${game.appId}/header.jpg`

    return (
        <div className="game-card" onClick={() => navigate(`/game/${game.id}`)}>
            <img
                src={imageUrl}
                alt={game.title}
                onError={e => { e.target.src = fallback }}
            />
            <div className="game-card-overlay">
                <span className="game-title">{game.title}</span>
                <span className={`game-status status-${userGame.status.toLowerCase()}`}>
          {userGame.status.replace('_', ' ')}
        </span>
            </div>
        </div>
    )
}