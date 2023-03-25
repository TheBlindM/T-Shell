import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router';
import { routeName } from '@/router';
import { fetchUserInfo } from '@/service';
import { useRouteStore, useAuthStore } from '@/store';
import { getToken, setRefreshToken, setToken, setUserInfo } from '@/utils';

const { VITE_APP_MULTI_USER_MODE } = import.meta.env;
/**
 * 动态路由
 */
export async function createDynamicRouteGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const route = useRouteStore();
  console.log(VITE_APP_MULTI_USER_MODE);
  if (VITE_APP_MULTI_USER_MODE === 'N') {
    // 初始化
    setToken('__TOKEN_SOYBEAN__');
    setRefreshToken('__REFRESH_TOKEN_SOYBEAN__');
    const { data } = await fetchUserInfo();
    if (data) {
      // 成功后把用户信息存储到缓存中
      setUserInfo(data);
      // 更新状态
      useAuthStore.userInfo = data;
      useAuthStore.token = '__TOKEN_SOYBEAN__';
    }
  }

  const isLogin = Boolean(getToken());

  // 初始化权限路由
  if (!route.isInitAuthRoute) {
    // 未登录情况下直接回到登录页，登录成功后再加载权限路由
    if (!isLogin) {
      if (to.name === routeName('login')) {
        next();
      } else {
        const redirect = to.fullPath;
        next({ name: routeName('login'), query: { redirect } });
      }
      return false;
    }

    await route.initAuthRoute();

    if (to.name === routeName('not-found-page')) {
      // 动态路由没有加载导致被not-found-page路由捕获，等待权限路由加载好了，回到之前的路由
      // 若路由是从根路由重定向过来的，重新回到根路由
      const ROOT_ROUTE_NAME: AuthRoute.RouteKey = 'root';
      const path = to.redirectedFrom?.name === ROOT_ROUTE_NAME ? '/' : to.fullPath;
      next({ path, replace: true, query: to.query, hash: to.hash });
      return false;
    }
  }

  // 权限路由已经加载，仍然未找到，重定向到not-found
  if (to.name === routeName('not-found-page')) {
    next({ name: routeName('not-found'), replace: true });
    return false;
  }

  return true;
}
