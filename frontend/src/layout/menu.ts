export interface MenuItem {
  label: string
  routeName: string
  adminOnly?: boolean
}

export const menuItems: MenuItem[] = [
  { label: '홈', routeName: 'main' },
  { label: '직원 관리', routeName: 'employee-list' },
  { label: '근무표', routeName: 'duty-roster', adminOnly: true },
]
