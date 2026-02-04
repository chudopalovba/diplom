import { useState, useEffect } from 'react'
import axios from 'axios'

function App() {
  const [message, setMessage] = useState('Loading...')

  useEffect(() => {
    axios.get('/api/hello')
      .then(res => setMessage(res.data.message))
      .catch(err => setMessage('Error: ' + err.message))
  }, [])

  return (
    <div className="app">
      <h1>{{PROJECT_NAME}}</h1>
      <p>{message}</p>
    </div>
  )
}

export default App