import './StatusCards.css'

const STATUSES = [
    { label: 'All Games', value: null },
    { label: 'Backlog', value: 'BACKLOG' },
    { label: 'In Progress', value: 'IN_PROGRESS' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Ignored', value: 'IGNORED' },
]

export default function StatusCards({ active, onSelect, counts }) {
    return (
        <div className="status-cards">
            {STATUSES.map(s => (
                <button
                    key={s.value}
                    className={`status-card ${active === s.value ? 'active' : ''}`}
                    onClick={() => onSelect(s.value)}
                >
                    <span className="status-label">{s.label}</span>
                    <span className="status-count">( {counts[s.value] ?? 0} )</span>
                </button>
            ))}
        </div>
    )
}