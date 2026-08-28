import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'compare',
    pathMatch: 'full'
  },
  {
    path: 'compare',
    loadComponent: () => import('./pages/compare/compare.component').then(m => m.CompareComponent)
  },
  {
    path: 'results/:comparisonId',
    loadComponent: () => import('./pages/results/results.component').then(m => m.ResultsComponent)
  }
];
