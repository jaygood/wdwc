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

const createIt = (parent, re) => {
  if (typeof re === 'string') {
      parent.appendChild(document.createTextNode(re));
      return re;
  }
  const p = removeChildren(re.props);
  if (typeof re.type === 'function') {
    return Object.assign(createChild(p, re), {
      children: createIt(parent, (new Shallow()).render(React.createElement(re.type, re.props)))
    });
  }

  const child = document.createElement(re.type)
  Object.keys(p).forEach(k => { child[typeof p[k] === 'function' ? k.toLowerCase() : k] = p[k]; });

  parent.appendChild(child);

  const children = Array.isArray(re.props.children) ? re.props.children : [re.props.children].filter(a => a);
  return createChild(
    children.length > 0 ? Object.assign({children: children.map(c => createIt(child, c))}, p) : p,
    re);
}

const result = Object.assign(createIt(document.getElementById('root2'), (new Shallow()).render(<App/>)), {name: 'App'});
document.getElementById('root3').innerHTML = JSON.stringify(result, null, 2);
