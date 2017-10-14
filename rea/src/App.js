import Shallow from 'react-test-renderer/shallow';
import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import logo from './logo.svg';
import './App.css';

window.React = React
window.ReactDOM = ReactDOM
window.Shallow = Shallow

function Something({onClick, caption}) {
  return <button onClick={onClick}>{caption}</button>;
}

function Two({children}) {
  return <p>{children}</p>
}

function One({children}) {
  return <Two>{children}</Two>
}

class Another extends Component {
  render() {
    return <div id="another">
      Something
      <One>
        <Something caption="no"/>
        <Something caption="yes" onClick={() => {
          const p = document.createElement('p')
          p.textContent = 'hello'
          document.getElementById('another').appendChild(p)
        }}/>
      </One>
    </div>
  }
}

export default class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Welcome to React</h1>
        </header>
        <p className="App-intro">
          To get started, edit <code>src/App.js</code> and save to reload.
        </p>
        <Something onClick={console.info} caption="This is caption"/>
        <Another/>
      </div>
    );
  }
}

const removeChildren = p => {
    const props = Object.assign({}, p)
    delete props.children
    return props;
}

const createChild = (props, re) => Object.keys(props).length > 0 ? {name: re.type.name || re.type, props} : {name: re.type.name || re.type}

const createIt = (parent, re, result) => {
  if (typeof re === 'string') {
      result.children = (result.children || []).concat(re);
      const child = document.createElement('text')
      child.textContent = re
      parent.appendChild(child)
      return
  }
  if (typeof re.type === 'function') {
    const props = removeChildren(re.props)
    const next = createChild(props, re);
    result.children = (result.children || []).concat(next);
    createIt(parent, (new Shallow()).render(React.createElement(re.type, re.props)), result.children[result.children.length - 1]);
    return
  }
  const child = document.createElement(re.type)
  const p = removeChildren(re.props)
  Object.keys(p).forEach(k => {
    child[typeof p[k] === 'function' ? k.toLowerCase() : k] = p[k]
  })

  result.children = (result.children || []).concat(createChild(p, re));

  const next = parent.appendChild(child)
  if (re.props.children) {
    if (Array.isArray(re.props.children)) {
      re.props.children.forEach(c => {
        createIt(next, c, result)
      })
    } else {
      child.textContent = re.props.children
    }
  }
}

const r = {name: 'App'}
createIt(document.getElementById('root2'), (new Shallow()).render(<App/>), r)
document.getElementById('root3').innerHTML = JSON.stringify(r, null, 2)
