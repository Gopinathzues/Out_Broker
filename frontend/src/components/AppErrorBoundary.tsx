import {
  Component,
  ErrorInfo,
  ReactNode,
} from "react";

import ErrorPage from "../pages/ErrorPage";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

class AppErrorBoundary extends Component<
  Props,
  State
> {
  state: State = {
    hasError: false,
  };

  static getDerivedStateFromError(): State {
    return {
      hasError: true,
    };
  }

  componentDidCatch(
    error: Error,
    errorInfo: ErrorInfo,
  ) {
    /*
     * Development logging only.
     *
     * Later this can be connected to an error
     * monitoring service without changing the UI.
     */
    console.error(
      "OutBroker application error:",
      error,
      errorInfo,
    );
  }

  handleRetry = () => {
    this.setState({
      hasError: false,
    });
  };

  render() {
    if (this.state.hasError) {
      return (
        <ErrorPage
          onRetry={this.handleRetry}
        />
      );
    }

    return this.props.children;
  }
}

export default AppErrorBoundary;