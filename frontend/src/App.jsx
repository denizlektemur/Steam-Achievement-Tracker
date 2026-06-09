import { BrowserRouter, Routes, Route } from 'react-router-dom'
import LibraryPage from './pages/LibraryPage'
import GameDetailPage from './pages/GameDetailPage'
import Navbar from './components/Navbar'

export default function App() {
  return (
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/" element={<LibraryPage />} />
          <Route path="/game/:gameId" element={<GameDetailPage />} />
        </Routes>
      </BrowserRouter>
  )
}