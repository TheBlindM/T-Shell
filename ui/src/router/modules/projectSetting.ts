const project: AuthRoute.Route = {
	name: 'project',
  path: '/project',
  component: 'basic',
  children: [
    {
      name: 'project_setting',
      path: '/project/setting',
      component: 'self',
      meta: {
        title: '设置',
        icon: 'mdi:vuejs',
        keepAlive: true,
      }
    }
  ],
  meta: {
    title: 'Setting',
    icon: 'carbon:document',
    order: 2
  }
};

export default project;
