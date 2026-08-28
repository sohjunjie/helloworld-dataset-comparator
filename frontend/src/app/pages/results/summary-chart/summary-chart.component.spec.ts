import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SummaryChartComponent } from './summary-chart.component';
import { ComparisonSummary } from '../../../models/comparison.model';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

describe('SummaryChartComponent', () => {
  let component: SummaryChartComponent;
  let fixture: ComponentFixture<SummaryChartComponent>;

  const mockSummary: ComparisonSummary = {
    id: 'test-comp-1',
    status: 'COMPLETED',
    createdAt: '2026-08-28T00:00:00Z',
    ds1FileName: 'ds1.csv',
    ds2FileName: 'ds2.csv',
    ds1RecordCount: 10000,
    ds2RecordCount: 10234,
    ds1FullyMatching: 9800,
    ds2FullyMatching: 9800,
    ds1NotMatching: 150,
    ds2NotMatching: 350,
    ds1MissingInDs2: 50,
    ds2MissingInDs1: 84
  };

  beforeEach(async () => {
    // Mock HTMLCanvasElement.prototype.getContext if needed in JSDOM
    if (!HTMLCanvasElement.prototype.getContext) {
      HTMLCanvasElement.prototype.getContext = (() => ({
        fillRect: () => {},
        clearRect: () => {},
        getImageData: () => ({ data: [] }),
        putImageData: () => {},
        createImageData: () => [],
        setTransform: () => {},
        drawImage: () => {},
        save: () => {},
        fillText: () => {},
        restore: () => {},
        beginPath: () => {},
        moveTo: () => {},
        lineTo: () => {},
        closePath: () => {},
        stroke: () => {},
        translate: () => {},
        scale: () => {},
        rotate: () => {},
        arc: () => {},
        fill: () => {},
        measureText: () => ({ width: 0 }),
        transform: () => {},
        rect: () => {},
        clip: () => {}
      })) as any;
    }

    await TestBed.configureTestingModule({
      imports: [SummaryChartComponent],
      providers: [provideCharts(withDefaultRegisterables())]
    }).compileComponents();

    fixture = TestBed.createComponent(SummaryChartComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('summary', mockSummary);
    fixture.detectChanges();
  });

  it('should create and render canvas chart', () => {
    expect(component).toBeTruthy();
    const canvas = fixture.nativeElement.querySelector('canvas');
    expect(canvas).toBeTruthy();
  });

  it('should compute chart data accurately based on summary input', () => {
    const data = component.barChartData();
    expect(data.labels).toEqual([
      'Fully Matching',
      'DS1 Mismatched',
      'DS2 Mismatched',
      'Missing from DS2',
      'Missing from DS1'
    ]);
    expect(data.datasets[0].data).toEqual([9800, 150, 350, 50, 84]);
  });

  it('should compute doughnut chart data accurately', () => {
    const data = component.doughnutChartData();
    expect(data.labels).toEqual([
      'Fully Matching',
      'DS1 Mismatched',
      'DS2 Mismatched',
      'Missing from DS2',
      'Missing from DS1'
    ]);
    expect(data.datasets[0].data).toEqual([9800, 150, 350, 50, 84]);
  });

  it('should toggle between bar and doughnut chart types', () => {
    expect(component.chartType()).toBe('bar');
    component.setChartType('doughnut');
    fixture.detectChanges();
    expect(component.chartType()).toBe('doughnut');

    const activeData = component.activeChartData();
    expect(activeData.datasets[0].data).toEqual([9800, 150, 350, 50, 84]);
  });
});
