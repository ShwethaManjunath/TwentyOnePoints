import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { SERVER_API_URL } from '../../app.constants';

import { JhiDateUtils } from 'ng-jhipster';

import { BloodPressure } from './blood-pressure.model';
import { ResponseWrapper, createRequestOption } from '../../shared';

@Injectable()
export class BloodPressureService {

    private resourceUrl = SERVER_API_URL + 'api/blood-pressures';
    private resourceSearchUrl = SERVER_API_URL + 'api/_search/blood-pressures';

    constructor(private http: Http, private dateUtils: JhiDateUtils) { }

    create(bloodPressure: BloodPressure): Observable<BloodPressure> {
        const copy = this.convert(bloodPressure);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    update(bloodPressure: BloodPressure): Observable<BloodPressure> {
        const copy = this.convert(bloodPressure);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    find(id: number): Observable<BloodPressure> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    query(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
            .map((res: Response) => this.convertResponse(res));
    }

    delete(id: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${id}`);
    }

    search(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options)
            .map((res: any) => this.convertResponse(res));
    }

    private convertResponse(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        const result = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            result.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return new ResponseWrapper(res.headers, result, res.status);
    }

    /**
     * Convert a returned JSON object to BloodPressure.
     */
    private convertItemFromServer(json: any): BloodPressure {
        const entity: BloodPressure = Object.assign(new BloodPressure(), json);
        entity.timestamp = this.dateUtils
            .convertLocalDateFromServer(json.timestamp);
        return entity;
    }

    /**
     * Convert a BloodPressure to a JSON which can be sent to the server.
     */
    private convert(bloodPressure: BloodPressure): BloodPressure {
        const copy: BloodPressure = Object.assign({}, bloodPressure);
        copy.timestamp = this.dateUtils
            .convertLocalDateToServer(bloodPressure.timestamp);
        return copy;
    }
}
