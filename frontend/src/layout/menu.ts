export interface MenuItem {
  label: string
  routeName: string
}

export const menuItems: MenuItem[] = [
  { label: '홈', routeName: 'main' },
  { label: '직원 관리', routeName: 'employee-list' },
]
