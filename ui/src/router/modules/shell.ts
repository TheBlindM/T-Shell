const shell: AuthRoute.Route = {
  name: 'shell',
  path: '/shell',
  component: 'basic',
  children: [
    {
      name: 'shell_info',
      path: '/shell/info',
      component: 'self',
      meta: {
        title: '首页',
        icon: 'icon-park-outline:analysis'
      }
    },
    {
      name: 'shell_terminal',
      path: '/shell/terminal',
      component: 'self',
      meta: {
        title: '连接中...',
        icon: 'mdi:vuejs',
        keepAlive: true,
        multiTab: true
      }
    }
  ],
  meta: {
    title: 'shell',
    icon: 'carbon:document',
    order: 2
  }
};

export default shell;
