import { Component } from 'react'

type Props = { children: React.ReactNode }
type State = { error: Error | null }

export class RootErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error) {
    return { error }
  }

  render() {
    if (this.state.error) {
      return (
        <div style={{ padding: 18, fontFamily: 'system-ui, sans-serif' }}>
          <h1 style={{ margin: 0, fontSize: 18 }}>화면 렌더링 에러</h1>
          <pre
            style={{
              marginTop: 12,
              padding: 12,
              borderRadius: 12,
              background: 'rgba(17, 24, 39, 0.9)',
              color: 'rgba(255, 255, 255, 0.9)',
              overflow: 'auto',
              fontSize: 12,
              lineHeight: 1.5,
            }}
          >
            {this.state.error.stack ?? this.state.error.message}
          </pre>
        </div>
      )
    }

    return this.props.children
  }
}

