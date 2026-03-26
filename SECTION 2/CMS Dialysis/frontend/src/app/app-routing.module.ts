import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AnalysisComponent } from './analysis/analysis.component';
import { SummaryComponent } from './summary/summary.component';

const routes: Routes = [
  { path: 'summary', component: SummaryComponent },
  { path: 'analysis', component: AnalysisComponent },
  { path: '', redirectTo: '/summary', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
