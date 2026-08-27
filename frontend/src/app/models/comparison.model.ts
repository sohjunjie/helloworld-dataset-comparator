export type ComparisonStatus = 'PENDING' | 'UPLOADED' | 'CONVERTING' | 'COMPARING' | 'COMPLETED' | 'FAILED';

export type DataSourceType = 'FILE_UPLOAD' | 'SQL_QUERY';

export interface ToleranceConfig {
  columnName: string;
  percentage: number;
}

export interface DatabaseConnectionConfig {
  host?: string;
  port?: number;
  database?: string;
  username?: string;
  password?: string;
}

export interface ComparisonRequest {
  ds1Type?: DataSourceType;
  ds1FileName?: string;
  ds2Type?: DataSourceType;
  ds2FileName?: string;
  keyColumns: string[];
  tolerances?: ToleranceConfig[];
  caseSensitive?: boolean;
  ds1Connection?: DatabaseConnectionConfig;
  ds2Connection?: DatabaseConnectionConfig;
  ds1Sql?: string;
  ds2Sql?: string;
  ds1Delimiter?: string;
  ds2Delimiter?: string;
}

export interface DatasetColumns {
  ds1: string[];
  ds2: string[];
}

export interface UploadConfigRequest {
  ds1Delimiter?: string;
  ds2Delimiter?: string;
  ds1Sql?: string;
  ds1Connection?: DatabaseConnectionConfig;
  ds2Sql?: string;
  ds2Connection?: DatabaseConnectionConfig;
}

export interface UploadDatasetOptions {
  ds1File?: File | null;
  ds2File?: File | null;
  ds1Delimiter?: string;
  ds2Delimiter?: string;
  ds1Sql?: string;
  ds1Connection?: DatabaseConnectionConfig;
  ds2Sql?: string;
  ds2Connection?: DatabaseConnectionConfig;
}

export interface UploadResponse {
  comparisonId: string;
  columns: DatasetColumns;
}

export interface ComparisonSummary {
  id: string;
  status: ComparisonStatus;
  createdAt: string;
  completedAt?: string;
  ds1Type?: DataSourceType;
  ds1FileName?: string;
  ds2Type?: DataSourceType;
  ds2FileName?: string;
  configJson?: string;
  ds1RecordCount?: number;
  ds2RecordCount?: number;
  ds1FullyMatching?: number;
  ds2FullyMatching?: number;
  ds1NotMatching?: number;
  ds2NotMatching?: number;
  ds1MissingInDs2?: number;
  ds2MissingInDs1?: number;
  errorMessage?: string;
}

export interface ProgressUpdate {
  stage: string;
  percent: number;
  message?: string;
}

export interface PagedResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface MismatchDetail {
  id?: string;
  rowNumberDs1?: number;
  rowNumberDs2?: number;
  dataDs1: Record<string, unknown>;
  dataDs2: Record<string, unknown>;
  differingColumns: string[];
}

export interface MissingDetail {
  id?: string;
  rowNumber?: number;
  data: Record<string, unknown>;
  direction?: 'DS1' | 'DS2';
}
